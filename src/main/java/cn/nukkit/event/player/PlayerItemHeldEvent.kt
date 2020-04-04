package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.item.Item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PlayerItemHeldEvent(player: Player?, item: Item, hotbarSlot: Int) : PlayerEvent(), Cancellable {
	val item: Item
	val inventorySlot: Int
		@Deprecated("") get

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.item = item
		inventorySlot = hotbarSlot
	}
}