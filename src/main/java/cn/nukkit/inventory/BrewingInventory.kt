package cn.nukkit.inventory

import cn.nukkit.blockentity.BlockEntityBrewingStand
import cn.nukkit.item.Item

class BrewingInventory(brewingStand: BlockEntityBrewingStand?) : ContainerInventory(brewingStand, InventoryType.BREWING_STAND) {
	override var holder: InventoryHolder?
		get() = field as BlockEntityBrewingStand?
		set(holder) {
			super.holder = holder
		}

	var ingredient: Item?
		get() = getItem(0)
		set(item) {
			setItem(0, item)
		}

	var fuel: Item?
		get() = getItem(4)
		set(fuel) {
			setItem(4, fuel)
		}

	override fun onSlotChange(index: Int, before: Item?, send: Boolean) {
		super.onSlotChange(index, before, send)
		if (index >= 1 && index <= 3) {
			this.holder.updateBlock()
		}
		this.holder.scheduleUpdate()
	}
}