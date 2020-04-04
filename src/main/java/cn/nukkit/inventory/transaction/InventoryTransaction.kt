package cn.nukkit.inventory.transaction

import cn.nukkit.Player
import cn.nukkit.event.inventory.InventoryClickEvent
import cn.nukkit.event.inventory.InventoryTransactionEvent
import cn.nukkit.inventory.Inventory
import cn.nukkit.inventory.PlayerInventory
import cn.nukkit.inventory.transaction.action.InventoryAction
import cn.nukkit.inventory.transaction.action.SlotChangeAction
import cn.nukkit.item.Item
import cn.nukkit.utils.MainLogger
import java.util.*
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.indices
import kotlin.collections.set

/**
 * @author CreeperFace
 */
open class InventoryTransaction @JvmOverloads constructor(source: Player?, actions: List<InventoryAction?>, init: Boolean = true) {
	var creationTime: Long = 0
		private set
	protected var hasExecuted = false
	var source: Player? = null
		protected set
	protected var inventories: MutableSet<Inventory?> = HashSet()
	protected var actions: MutableSet<InventoryAction?> = HashSet()
	protected fun init(source: Player?, actions: List<InventoryAction?>) {
		creationTime = System.currentTimeMillis()
		this.source = source
		for (action in actions) {
			addAction(action)
		}
	}

	fun getInventories(): Set<Inventory?> {
		return inventories
	}

	fun getActions(): Set<InventoryAction?> {
		return actions
	}

	fun addAction(action: InventoryAction?) {
		if (!actions.contains(action)) {
			actions.add(action)
			action!!.onAddToTransaction(this)
		} else {
			throw RuntimeException("Tried to add the same action to a transaction twice")
		}
	}

	/**
	 * This method should not be used by plugins, it's used to add tracked inventories for InventoryActions
	 * involving inventories.
	 *
	 * @param inventory to add
	 */
	fun addInventory(inventory: Inventory?) {
		inventories.add(inventory)
	}

	protected fun matchItems(needItems: MutableList<Item?>, haveItems: MutableList<Item?>): Boolean {
		for (action in actions) {
			if (action!!.targetItem.id != Item.AIR) {
				needItems.add(action.targetItem)
			}
			if (!action.isValid(source)) {
				return false
			}
			if (action.sourceItem.id != Item.AIR) {
				haveItems.add(action.sourceItem)
			}
		}
		for (needItem in ArrayList(needItems)) {
			for (haveItem in ArrayList(haveItems)) {
				if (needItem == haveItem) {
					val amount = Math.min(haveItem!!.getCount(), needItem!!.getCount())
					needItem.setCount(needItem.getCount() - amount)
					haveItem.setCount(haveItem.getCount() - amount)
					if (haveItem.getCount() == 0) {
						haveItems.remove(haveItem)
					}
					if (needItem.getCount() == 0) {
						needItems.remove(needItem)
						break
					}
				}
			}
		}
		return haveItems.isEmpty() && needItems.isEmpty()
	}

	protected open fun sendInventories() {
		for (inventory in inventories) {
			inventory!!.sendContents(source!!)
			if (inventory is PlayerInventory) {
				inventory.sendArmorContents(source)
			}
		}
	}

	/**
	 * Iterates over SlotChangeActions in this transaction and compacts any which refer to the same inventorySlot in the same
	 * inventory so they can be correctly handled.
	 *
	 *
	 * Under normal circumstances, the same inventorySlot would never be changed more than once in a single transaction. However,
	 * due to the way things like the crafting grid are "implemented" in MCPE 1.2 (a.k.a. hacked-in), we may get
	 * multiple inventorySlot changes referring to the same inventorySlot in a single transaction. These multiples are not even guaranteed
	 * to be in the correct order (inventorySlot splitting in the crafting grid for example, causes the actions to be sent in the
	 * wrong order), so this method also tries to chain them into order.
	 *
	 *
	 * @return successful
	 */
	protected fun squashDuplicateSlotChanges(): Boolean {
		val slotChanges: MutableMap<Int, MutableList<SlotChangeAction>> = HashMap()
		for (action in actions) {
			if (action is SlotChangeAction) {
				val hash = Objects.hash(action.inventory, action.slot)
				var list = slotChanges[hash]
				if (list == null) {
					list = ArrayList()
				}
				list.add(action)
				slotChanges[hash] = list
			}
		}
		for ((hash, list) in ArrayList<Map.Entry<Int, List<SlotChangeAction>>>(slotChanges.entries)) {
			if (list.size == 1) { //No need to compact inventorySlot changes if there is only one on this inventorySlot
				slotChanges.remove(hash)
				continue
			}
			val originalList: List<SlotChangeAction> = ArrayList(list)
			var originalAction: SlotChangeAction? = null
			var lastTargetItem: Item? = null
			for (i in list.indices) {
				val action = list[i]
				if (action.isValid(source)) {
					originalAction = action
					lastTargetItem = action.targetItem
					list.removeAt(i)
					break
				}
			}
			if (originalAction == null) {
				return false //Couldn't find any actions that had a source-item matching the current inventory inventorySlot
			}
			var sortedThisLoop: Int
			do {
				sortedThisLoop = 0
				for (i in list.indices) {
					val action = list[i]
					val actionSource = action.sourceItem
					if (actionSource!!.equalsExact(lastTargetItem)) {
						lastTargetItem = action.targetItem
						list.removeAt(i)
						sortedThisLoop++
					} else if (actionSource == lastTargetItem) {
						lastTargetItem.count -= actionSource.count
						list.removeAt(i)
						if (lastTargetItem.count == 0) sortedThisLoop++
					}
				}
			} while (sortedThisLoop > 0)
			if (list.size > 0) { //couldn't chain all the actions together
				MainLogger.getLogger().debug("Failed to compact " + originalList.size + " actions for " + source!!.getName())
				return false
			}
			for (action in originalList) {
				actions.remove(action)
			}
			addAction(SlotChangeAction(originalAction.inventory, originalAction.slot, originalAction.sourceItem, lastTargetItem))
			MainLogger.getLogger().debug("Successfully compacted " + originalList.size + " actions for " + source!!.getName())
		}
		return true
	}

	open fun canExecute(): Boolean {
		squashDuplicateSlotChanges()
		val haveItems: MutableList<Item?> = ArrayList()
		val needItems: MutableList<Item?> = ArrayList()
		return matchItems(needItems, haveItems) && actions.size > 0 && haveItems.size == 0 && needItems.size == 0
	}

	protected open fun callExecuteEvent(): Boolean {
		val ev = InventoryTransactionEvent(this)
		source!!.getServer().pluginManager.callEvent(ev)
		var from: SlotChangeAction? = null
		var to: SlotChangeAction? = null
		var who: Player? = null
		for (action in actions) {
			if (action !is SlotChangeAction) {
				continue
			}
			val slotChange = action
			if (slotChange.inventory is PlayerInventory) {
				who = slotChange.inventory.holder as Player
			}
			if (from == null) {
				from = slotChange
			} else {
				to = slotChange
			}
		}
		if (who != null && to != null) {
			if (from!!.targetItem.getCount() > from.sourceItem.getCount()) {
				from = to
			}
			val ev2 = InventoryClickEvent(who, from.inventory, from.slot, from.sourceItem, from.targetItem)
			source!!.getServer().pluginManager.callEvent(ev2)
			if (ev2.isCancelled) {
				return false
			}
		}
		return !ev.isCancelled
	}

	open fun execute(): Boolean {
		if (hasExecuted() || !canExecute()) {
			sendInventories()
			return false
		}
		if (!callExecuteEvent()) {
			sendInventories()
			return true
		}
		for (action in actions) {
			if (!action!!.onPreExecute(source)) {
				sendInventories()
				return true
			}
		}
		for (action in actions) {
			if (action!!.execute(source)) {
				action.onExecuteSuccess(source)
			} else {
				action.onExecuteFail(source)
			}
		}
		hasExecuted = true
		return true
	}

	fun hasExecuted(): Boolean {
		return hasExecuted
	}

	init {
		if (init) {
			init(source, actions)
		}
	}
}