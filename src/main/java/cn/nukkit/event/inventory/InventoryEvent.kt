package cn.nukkit.event.inventory

import cn.nukkit.Player
import cn.nukkit.event.Event
import cn.nukkit.inventory.Inventory

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class InventoryEvent(val inventory: Inventory) : Event() {

	val viewers: Array<Player>
		get() = inventory.viewers.toTypedArray()

}