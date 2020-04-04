package cn.nukkit.event.inventory

import cn.nukkit.entity.item.EntityItem
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.inventory.Inventory

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class InventoryPickupItemEvent(inventory: Inventory, val item: EntityItem) : InventoryEvent(inventory), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}