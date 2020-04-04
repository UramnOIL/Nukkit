package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.HandlerList
import cn.nukkit.lang.TextContainer

class PlayerJoinEvent : PlayerEvent {
	var joinMessage: TextContainer

	constructor(player: Player?, joinMessage: TextContainer) {
		this.player = player
		this.joinMessage = joinMessage
	}

	constructor(player: Player?, joinMessage: String?) {
		this.player = player
		this.joinMessage = TextContainer(joinMessage)
	}

	fun setJoinMessage(joinMessage: String?) {
		this.joinMessage = TextContainer(joinMessage)
	}

	companion object {
		val handlers = HandlerList()
	}
}