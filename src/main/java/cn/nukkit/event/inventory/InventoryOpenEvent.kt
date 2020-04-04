package cn.nukkit.event.inventory

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.inventory.Inventory

/**
 * author: Box
 * Nukkit Project
 */
class InventoryOpenEvent(inventory: Inventory, val player: Player) : InventoryEvent(inventory), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}