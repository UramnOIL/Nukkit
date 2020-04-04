package cn.nukkit.event.inventory

import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.inventory.Inventory
import cn.nukkit.inventory.InventoryHolder
import cn.nukkit.item.Item

/**
 * @author CreeperFace
 *
 *
 * Called when inventory transaction is not caused by a player
 */
class InventoryMoveItemEvent(from: Inventory, val targetInventory: Inventory, val source: InventoryHolder, var item: Item, val action: Action) : InventoryEvent(from), Cancellable {

	enum class Action {
		SLOT_CHANGE,  //transaction between 2 inventories
		PICKUP, DROP, DISPENSE
	}

	companion object {
		val handlers = HandlerList()
	}

}