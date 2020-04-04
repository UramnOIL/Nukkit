package cn.nukkit.inventory

import cn.nukkit.entity.item.EntityMinecartChest

class MinecartChestInventory(minecart: EntityMinecartChest?) : ContainerInventory(minecart, InventoryType.MINECART_CHEST) {
	override var holder: InventoryHolder?
		get() = field as EntityMinecartChest?
		set(holder) {
			super.holder = holder
		}
}