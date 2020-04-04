package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * Called when the player logs in, before things have been set up
 */
class PlayerPreLoginEvent(player: Player?, kickMessage: String) : PlayerEvent(), Cancellable {
	var kickMessage: String

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.kickMessage = kickMessage
	}
}