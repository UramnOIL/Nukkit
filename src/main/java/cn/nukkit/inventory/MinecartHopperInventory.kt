package cn.nukkit.inventory

import cn.nukkit.entity.item.EntityMinecartHopper

class MinecartHopperInventory(minecart: EntityMinecartHopper?) : ContainerInventory(minecart, InventoryType.MINECART_HOPPER) {
	override var holder: InventoryHolder?
		get() = super.getHolder() as EntityMinecartHopper
		set(holder) {
			super.holder = holder
		}
}