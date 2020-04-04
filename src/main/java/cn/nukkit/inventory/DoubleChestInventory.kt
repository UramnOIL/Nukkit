package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.item.Item
import cn.nukkit.network.protocol.BlockEventPacket
import cn.nukkit.network.protocol.InventorySlotPacket
import cn.nukkit.network.protocol.LevelSoundEventPacket
import java.util.*
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class DoubleChestInventory(left: BlockEntityChest, right: BlockEntityChest) : ContainerInventory(null, InventoryType.DOUBLE_CHEST), InventoryHolder {
	val leftSide: ChestInventory?
	val rightSide: ChestInventory?
	override val inventory: Inventory
		get() = this

	override var holder: InventoryHolder?
		get() = leftSide.getHolder()
		set(holder) {
			super.holder = holder
		}

	override fun getItem(index: Int): Item? {
		return if (index < leftSide.getSize()) leftSide!!.getItem(index) else rightSide!!.getItem(index - rightSide.getSize())
	}

	override fun setItem(index: Int, item: Item?, send: Boolean): Boolean {
		return if (index < leftSide.getSize()) leftSide!!.setItem(index, item, send) else rightSide!!.setItem(index - rightSide.getSize(), item, send)
	}

	override fun clear(index: Int): Boolean {
		return if (index < leftSide.getSize()) leftSide!!.clear(index) else rightSide!!.clear(index - rightSide.getSize())
	}

	override fun getContents(): Map<Int?, Item?>? {
		val contents: MutableMap<Int?, Item?> = HashMap()
		for (i in 0 until getSize()) {
			contents[i] = getItem(i)
		}
		return contents
	}

	override fun setContents(items: Map<Int?, Item?>) {
		var items = items
		if (items.size > size) {
			val newItems: MutableMap<Int?, Item?> = HashMap()
			for (i in 0 until size) {
				newItems[i] = items[i]
			}
			items = newItems
		}
		for (i in 0 until size) {
			if (!items.containsKey(i)) {
				if (i < leftSide!!.size) {
					if (leftSide.slots.containsKey(i)) {
						this.clear(i)
					}
				} else if (rightSide!!.slots.containsKey(i - leftSide.size)) {
					this.clear(i)
				}
			} else if (!this.setItem(i, items[i])) {
				this.clear(i)
			}
		}
	}

	override fun onOpen(who: Player) {
		super.onOpen(who)
		leftSide!!.viewers.add(who)
		rightSide!!.viewers.add(who)
		if (getViewers()!!.size == 1) {
			val pk1 = BlockEventPacket()
			pk1.x = leftSide.getHolder().getX()
			pk1.y = leftSide.getHolder().getY()
			pk1.z = leftSide.getHolder().getZ()
			pk1.case1 = 1
			pk1.case2 = 2
			var level = leftSide.getHolder().getLevel()
			if (level != null) {
				level.addLevelSoundEvent(leftSide.getHolder().add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_CHEST_OPEN)
				level.addChunkPacket(leftSide.getHolder().getX() as Int shr 4, leftSide.getHolder().getZ() as Int shr 4, pk1)
			}
			val pk2 = BlockEventPacket()
			pk2.x = rightSide.getHolder().getX()
			pk2.y = rightSide.getHolder().getY()
			pk2.z = rightSide.getHolder().getZ()
			pk2.case1 = 1
			pk2.case2 = 2
			level = rightSide.getHolder().getLevel()
			if (level != null) {
				level.addLevelSoundEvent(rightSide.getHolder().add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_CHEST_OPEN)
				level.addChunkPacket(rightSide.getHolder().getX() as Int shr 4, rightSide.getHolder().getZ() as Int shr 4, pk2)
			}
		}
	}

	override fun onClose(who: Player) {
		if (getViewers()!!.size == 1) {
			val pk1 = BlockEventPacket()
			pk1.x = rightSide.getHolder().getX()
			pk1.y = rightSide.getHolder().getY()
			pk1.z = rightSide.getHolder().getZ()
			pk1.case1 = 1
			pk1.case2 = 0
			var level = rightSide.getHolder().getLevel()
			if (level != null) {
				level.addLevelSoundEvent(rightSide.getHolder().add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_CHEST_CLOSED)
				level.addChunkPacket(rightSide.getHolder().getX() as Int shr 4, rightSide.getHolder().getZ() as Int shr 4, pk1)
			}
			val pk2 = BlockEventPacket()
			pk2.x = leftSide.getHolder().getX()
			pk2.y = leftSide.getHolder().getY()
			pk2.z = leftSide.getHolder().getZ()
			pk2.case1 = 1
			pk2.case2 = 0
			level = leftSide.getHolder().getLevel()
			if (level != null) {
				level.addLevelSoundEvent(leftSide.getHolder().add(0.5, 0.5, 0.5), LevelSoundEventPacket.SOUND_CHEST_CLOSED)
				level.addChunkPacket(leftSide.getHolder().getX() as Int shr 4, leftSide.getHolder().getZ() as Int shr 4, pk2)
			}
		}
		leftSide!!.viewers.remove(who)
		rightSide!!.viewers.remove(who)
		super.onClose(who)
	}

	fun sendSlot(inv: Inventory, index: Int, vararg players: Player) {
		val pk = InventorySlotPacket()
		pk.slot = if (inv === rightSide) leftSide.getSize() + index else index
		pk.item = inv.getItem(index)!!.clone()
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

	init {
		this.holder = this
		leftSide = left.realInventory
		leftSide.setDoubleInventory(this)
		rightSide = right.realInventory
		rightSide.setDoubleInventory(this)
		val items: MutableMap<Int?, Item?> = HashMap()
		// First we add the items from the left chest
		for (idx in 0 until leftSide.getSize()) {
			if (leftSide!!.contents!!.containsKey(idx)) { // Don't forget to skip empty slots!
				items[idx] = leftSide.contents!![idx]
			}
		}
		// And them the items from the right chest
		for (idx in 0 until rightSide.getSize()) {
			if (rightSide!!.contents!!.containsKey(idx)) { // Don't forget to skip empty slots!
				items[idx + leftSide.getSize()] = rightSide.contents!![idx] // idx + this.left.getSize() so we don't overlap left chest items
			}
		}
		setContents(items)
	}
}