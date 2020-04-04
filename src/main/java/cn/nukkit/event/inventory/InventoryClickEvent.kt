package cn.nukkit.event.inventory

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.inventory.Inventory
import cn.nukkit.item.Item

/**
 * author: boybook
 * Nukkit Project
 */
class InventoryClickEvent(val player: Player, inventory: Inventory, val slot: Int, val sourceItem: Item, val heldItem: Item) : InventoryEvent(inventory), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}