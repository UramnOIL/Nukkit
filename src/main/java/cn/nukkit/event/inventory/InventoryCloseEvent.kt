package cn.nukkit.event.inventory

import cn.nukkit.Player
import cn.nukkit.event.HandlerList
import cn.nukkit.inventory.Inventory

/**
 * author: Box
 * Nukkit Project
 */
class InventoryCloseEvent(inventory: Inventory, val player: Player) : InventoryEvent(inventory) {

	companion object {
		val handlers = HandlerList()
	}

}