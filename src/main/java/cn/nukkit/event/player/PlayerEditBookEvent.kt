package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.item.Item
import cn.nukkit.network.protocol.BookEditPacket

class PlayerEditBookEvent(player: Player?, oldBook: Item, newBook: Item, action: BookEditPacket.Action) : PlayerEvent(), Cancellable {
	val oldBook: Item
	val action: BookEditPacket.Action
	var newBook: Item

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.oldBook = oldBook
		this.newBook = newBook
		this.action = action
	}
}