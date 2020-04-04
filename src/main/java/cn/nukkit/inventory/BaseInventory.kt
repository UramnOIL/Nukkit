package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityInventoryChangeEvent
import cn.nukkit.event.inventory.InventoryOpenEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.network.protocol.InventoryContentPacket
import cn.nukkit.network.protocol.InventorySlotPacket
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BaseInventory @JvmOverloads constructor(override var holder: InventoryHolder?, override val type: InventoryType, items: Map<Int?, Item?> = HashMap(), overrideSize: Int? = null, overrideTitle: String? = null) : Inventory {
	override var maxStackSize: Int = Inventory.Companion.MAX_STACK
	override var size = 0
	override val name: String?
	override var title: String? = null
	val slots: MutableMap<Int?, Item?> = HashMap()
	override val viewers: MutableSet<Player?> = HashSet()

	override fun getItem(index: Int): Item? {
		return if (slots.containsKey(index)) slots[index]!!.clone() else ItemBlock(get(BlockID.AIR), null, 0)
	}

	override fun getContents(): Map<Int?, Item?>? {
		return HashMap(slots)
	}

	override fun setContents(items: Map<Int?, Item?>) {
		var items = items
		if (items.size > size) {
			var newItems = TreeMap<Int?, Item?>()
			for ((key, value) in items) {
				newItems[key] = value
			}
			items = newItems
			newItems = TreeMap()
			var i = 0
			for ((key, value) in items) {
				newItems[key] = value
				i++
				if (i >= size) {
					break
				}
			}
			items = newItems
		}
		for (i in 0 until size) {
			if (!items.containsKey(i)) {
				if (slots.containsKey(i)) {
					this.clear(i)
				}
			} else {
				if (!this.setItem(i, items[i])) {
					this.clear(i)
				}
			}
		}
	}

	override fun setItem(index: Int, item: Item?, send: Boolean): Boolean {
		var item = item
		item = item!!.clone()
		if (index < 0 || index >= size) {
			return false
		} else if (item.id == 0 || item.getCount() <= 0) {
			return this.clear(index, send)
		}
		val holder = holder
		if (holder is Entity) {
			val ev = EntityInventoryChangeEvent(holder as Entity?, getItem(index)!!, item, index)
			Server.instance!!.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				this.sendSlot(index, getViewers())
				return false
			}
			item = ev.newItem
		}
		if (holder is BlockEntity) {
			(holder as BlockEntity).setDirty()
		}
		val old = getItem(index)
		slots[index] = item.clone()
		onSlotChange(index, old, send)
		return true
	}

	override fun contains(item: Item): Boolean {
		var count = Math.max(1, item.getCount())
		val checkDamage = item.hasMeta() && item.damage >= 0
		val checkTag = item.compoundTag != null
		for (i in this.contents!!.values) {
			if (item.equals(i, checkDamage, checkTag)) {
				count -= i!!.getCount()
				if (count <= 0) {
					return true
				}
			}
		}
		return false
	}

	override fun all(item: Item): Map<Int?, Item?> {
		val slots: MutableMap<Int?, Item?> = HashMap()
		val checkDamage = item.hasMeta() && item.damage >= 0
		val checkTag = item.compoundTag != null
		for ((key, value) in this.contents!!) {
			if (item.equals(value, checkDamage, checkTag)) {
				slots[key] = value
			}
		}
		return slots
	}

	override fun remove(item: Item) {
		val checkDamage = item.hasMeta()
		val checkTag = item.compoundTag != null
		for ((key, value) in this.contents!!) {
			if (item.equals(value, checkDamage, checkTag)) {
				this.clear(key!!)
			}
		}
	}

	override fun first(item: Item, exact: Boolean): Int {
		val count = Math.max(1, item.getCount())
		val checkDamage = item.hasMeta()
		val checkTag = item.compoundTag != null
		for ((key, value) in this.contents!!) {
			if (item.equals(value, checkDamage, checkTag) && (value!!.getCount() == count || !exact && value!!.getCount() > count)) {
				return key
			}
		}
		return -1
	}

	override fun firstEmpty(item: Item?): Int {
		for (i in 0 until size) {
			if (getItem(i)!!.id == Item.AIR) {
				return i
			}
		}
		return -1
	}

	override fun decreaseCount(slot: Int) {
		val item = getItem(slot)
		if (item!!.getCount() > 0) {
			item.count--
			this.setItem(slot, item)
		}
	}

	override fun canAddItem(item: Item): Boolean {
		var item = item
		item = item.clone()
		val checkDamage = item.hasMeta()
		val checkTag = item.compoundTag != null
		for (i in 0 until size) {
			val slot = getItem(i)
			if (item.equals(slot, checkDamage, checkTag)) {
				var diff: Int
				if (slot!!.maxStackSize - slot.getCount().also { diff = it } > 0) {
					item.setCount(item.getCount() - diff)
				}
			} else if (slot!!.id == Item.AIR) {
				item.setCount(item.getCount() - maxStackSize)
			}
			if (item.getCount() <= 0) {
				return true
			}
		}
		return false
	}

	override fun addItem(vararg slots: Item): Array<Item?> {
		val itemSlots: MutableList<Item> = ArrayList()
		for (slot in slots) {
			if (slot.id != 0 && slot.getCount() > 0) {
				itemSlots.add(slot.clone())
			}
		}
		val emptySlots: MutableList<Int> = ArrayList()
		for (i in 0 until size) {
			val item = getItem(i)
			if (item!!.id == Item.AIR || item.getCount() <= 0) {
				emptySlots.add(i)
			}
			for (slot in ArrayList(itemSlots)) {
				if (slot == item && item.getCount() < item.maxStackSize) {
					var amount = Math.min(item.maxStackSize - item.getCount(), slot.getCount())
					amount = Math.min(amount, maxStackSize)
					if (amount > 0) {
						slot.setCount(slot.count - amount)
						item.setCount(item.count + amount)
						this.setItem(i, item)
						if (slot.getCount() <= 0) {
							itemSlots.remove(slot)
						}
					}
				}
			}
			if (itemSlots.isEmpty()) {
				break
			}
		}
		if (!itemSlots.isEmpty() && !emptySlots.isEmpty()) {
			for (slotIndex in emptySlots) {
				if (!itemSlots.isEmpty()) {
					val slot = itemSlots[0]
					var amount = Math.min(slot.maxStackSize, slot.getCount())
					amount = Math.min(amount, maxStackSize)
					slot.setCount(slot.getCount() - amount)
					val item = slot.clone()
					item.setCount(amount)
					this.setItem(slotIndex, item)
					if (slot.getCount() <= 0) {
						itemSlots.remove(slot)
					}
				}
			}
		}
		return itemSlots.toTypedArray()
	}

	override fun removeItem(vararg slots: Item): Array<Item?> {
		val itemSlots: MutableList<Item> = ArrayList()
		for (slot in slots) {
			if (slot.id != 0 && slot.getCount() > 0) {
				itemSlots.add(slot.clone())
			}
		}
		for (i in 0 until size) {
			val item = getItem(i)
			if (item!!.id == Item.AIR || item.getCount() <= 0) {
				continue
			}
			for (slot in ArrayList(itemSlots)) {
				if (slot.equals(item, item.hasMeta(), item.compoundTag != null)) {
					val amount = Math.min(item.getCount(), slot.getCount())
					slot.setCount(slot.getCount() - amount)
					item.setCount(item.getCount() - amount)
					this.setItem(i, item)
					if (slot.getCount() <= 0) {
						itemSlots.remove(slot)
					}
				}
			}
			if (itemSlots.size == 0) {
				break
			}
		}
		return itemSlots.toTypedArray()
	}

	override fun clear(index: Int, send: Boolean): Boolean {
		if (slots.containsKey(index)) {
			var item: Item = ItemBlock(get(BlockID.AIR), null, 0)
			val old = slots[index]
			val holder = holder
			if (holder is Entity) {
				val ev = EntityInventoryChangeEvent(holder as Entity?, old!!, item, index)
				Server.instance!!.pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					this.sendSlot(index, getViewers())
					return false
				}
				item = ev.newItem
			}
			if (holder is BlockEntity) {
				(holder as BlockEntity).setDirty()
			}
			if (item.id != Item.AIR) {
				slots[index] = item.clone()
			} else {
				slots.remove(index)
			}
			onSlotChange(index, old, send)
		}
		return true
	}

	override fun clearAll() {
		for (index in this.contents!!.keys) {
			this.clear(index!!)
		}
	}

	override fun getViewers(): Set<Player?>? {
		return viewers
	}

	override fun open(who: Player): Boolean {
		val ev = InventoryOpenEvent(this, who)
		who.getServer().pluginManager.callEvent(ev)
		if (ev.isCancelled) {
			return false
		}
		onOpen(who)
		return true
	}

	override fun close(who: Player) {
		onClose(who)
	}

	override fun onOpen(who: Player) {
		viewers.add(who)
	}

	override fun onClose(who: Player) {
		viewers.remove(who)
	}

	override fun onSlotChange(index: Int, before: Item?, send: Boolean) {
		if (send) {
			this.sendSlot(index, getViewers())
		}
	}

	override fun sendContents(player: Player?) {
		this.sendContents(*arrayOf(player))
	}

	override fun sendContents(vararg players: Player) {
		val pk = InventoryContentPacket()
		pk.slots = arrayOfNulls(size)
		for (i in 0 until size) {
			pk.slots[i] = getItem(i)
		}
		for (player in players) {
			val id = player.getWindowId(this)
			if (id == -1 || !player.spawned) {
				close(player)
				continue
			}
			pk.inventoryId = id
			player.dataPacket(pk)
		}
	}

	override val isFull: Boolean
		get() {
			if (slots.size < size) {
				return false
			}
			for (item in slots.values) {
				if (item == null || item.id == 0 || item.getCount() < item.maxStackSize || item.getCount() < maxStackSize) {
					return false
				}
			}
			return true
		}

	override val isEmpty: Boolean
		get() {
			if (maxStackSize <= 0) {
				return false
			}
			for (item in slots.values) {
				if (item != null && item.id != 0 && item.getCount() > 0) {
					return false
				}
			}
			return true
		}

	fun getFreeSpace(item: Item): Int {
		val maxStackSize = Math.min(item.maxStackSize, maxStackSize)
		var space = (size - slots.size) * maxStackSize
		for (slot in this.contents!!.values) {
			if (slot == null || slot.id == 0) {
				space += maxStackSize
				continue
			}
			if (slot.equals(item, true, true)) {
				space += maxStackSize - slot.getCount()
			}
		}
		return space
	}

	override fun sendContents(players: Collection<Player?>?) {
		this.sendContents(*players!!.toTypedArray())
	}

	override fun sendSlot(index: Int, player: Player?) {
		this.sendSlot(index, *arrayOf(player))
	}

	override fun sendSlot(index: Int, vararg players: Player) {
		val pk = InventorySlotPacket()
		pk.slot = index
		pk.item = getItem(index)!!.clone()
		for (player in players) {
			val id = player.getWindowId(this)
			if (id == -1) {
				close(player)
				continue
			}
			pk.inventoryId = id
			player.dataPacket(pk)
		}
	}

	override fun sendSlot(index: Int, players: Collection<Player?>?) {
		this.sendSlot(index, *players!!.toTypedArray())
	}

	override fun getType(): InventoryType? {
		return type
	}

	init {
		if (overrideSize != null) {
			size = overrideSize
		} else {
			size = type.defaultSize
		}
		if (overrideTitle != null) {
			title = overrideTitle
		} else {
			title = type.defaultTitle
		}
		name = type.defaultTitle
		if (this !is DoubleChestInventory) {
			setContents(items)
		}
	}
}