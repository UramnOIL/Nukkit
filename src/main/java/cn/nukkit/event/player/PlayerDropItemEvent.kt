package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.item.Item

class PlayerDropItemEvent(player: Player?, drop: Item) : PlayerEvent(), Cancellable {
	val item: Item

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		item = drop
	}
}