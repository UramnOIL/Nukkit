package cn.nukkit.event.inventory

import cn.nukkit.event.Cancellable
import cn.nukkit.event.Event
import cn.nukkit.event.HandlerList
import cn.nukkit.inventory.transaction.InventoryTransaction

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class InventoryTransactionEvent(val transaction: InventoryTransaction) : Event(), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}