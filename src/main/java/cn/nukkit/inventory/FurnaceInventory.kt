package cn.nukkit.inventory

import cn.nukkit.blockentity.BlockEntityFurnace
import cn.nukkit.item.Item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class FurnaceInventory(furnace: BlockEntityFurnace?) : ContainerInventory(furnace, InventoryType.FURNACE) {
	override var holder: InventoryHolder?
		get() = field as BlockEntityFurnace?
		set(holder) {
			super.holder = holder
		}

	val result: Item?
		get() = getItem(2)

	val fuel: Item?
		get() = getItem(1)

	val smelting: Item?
		get() = getItem(0)

	fun setResult(item: Item?): Boolean {
		return this.setItem(2, item)
	}

	fun setFuel(item: Item?): Boolean {
		return this.setItem(1, item)
	}

	fun setSmelting(item: Item?): Boolean {
		return this.setItem(0, item)
	}

	override fun onSlotChange(index: Int, before: Item?, send: Boolean) {
		super.onSlotChange(index, before, send)
		this.holder.scheduleUpdate()
	}
}