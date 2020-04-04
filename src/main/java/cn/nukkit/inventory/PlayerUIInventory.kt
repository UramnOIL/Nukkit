package cn.nukkit.inventory

import cn.nukkit.Player
import cn.nukkit.network.protocol.InventoryContentPacket
import cn.nukkit.network.protocol.InventorySlotPacket
import cn.nukkit.network.protocol.types.ContainerIds
import java.util.*

class PlayerUIInventory(player: Player?) : BaseInventory(player, InventoryType.UI, HashMap(), 51) {
	override val holder: Player?
		private set(holder) {
			super.holder = holder
		}
	val cursorInventory: PlayerCursorInventory
	val craftingGrid: CraftingGrid
	val bigCraftingGrid: BigCraftingGrid

	override fun sendSlot(index: Int, vararg target: Player) {
		val pk = InventorySlotPacket()
		pk.slot = index
		pk.item = getItem(index)
		for (p in target) {
			if (p === this.holder) {
				pk.inventoryId = ContainerIds.UI
				p.dataPacket(pk)
			} else {
				var id: Int
				if (p.getWindowId(this).also { id = it } == ContainerIds.NONE) {
					close(p)
					continue
				}
				pk.inventoryId = id
				p.dataPacket(pk)
			}
		}
	}

	override fun sendContents(vararg target: Player) {
		val pk = InventoryContentPacket()
		pk.slots = arrayOfNulls(this.size)
		for (i in 0 until this.size) {
			pk.slots[i] = getItem(i)
		}
		for (p in target) {
			if (p === this.holder) {
				pk.inventoryId = ContainerIds.UI
				p.dataPacket(pk)
			} else {
				var id: Int
				if (p.getWindowId(this).also { id = it } == ContainerIds.NONE) {
					close(p)
					continue
				}
				pk.inventoryId = id
				p.dataPacket(pk)
			}
		}
	}

	override var size: Int
		get() = 51
		set(size) {
			throw UnsupportedOperationException("UI size is immutable")
		}

	init {
		this.holder = player
		cursorInventory = PlayerCursorInventory(this)
		craftingGrid = CraftingGrid(this)
		bigCraftingGrid = BigCraftingGrid(this)
	}
}