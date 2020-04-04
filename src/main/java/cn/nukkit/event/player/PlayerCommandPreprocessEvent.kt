package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class PlayerCommandPreprocessEvent(player: Player?, message: String?) : PlayerMessageEvent(), Cancellable {
	override var player: Player?
		get() = super.player

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.message = message
	}
}