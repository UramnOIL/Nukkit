package cn.nukkit.inventory

import cn.nukkit.blockentity.BlockEntityHopper

/**
 * Created by CreeperFace on 8.5.2017.
 */
class HopperInventory(hopper: BlockEntityHopper?) : ContainerInventory(hopper, InventoryType.HOPPER) {
	override var holder: InventoryHolder?
		get() = super.getHolder() as BlockEntityHopper
		set(holder) {
			super.holder = holder
		}
}