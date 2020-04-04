package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class PlayerToggleSneakEvent(player: Player?, isSneaking: Boolean) : PlayerEvent(), Cancellable {
	val isSneaking: Boolean

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.isSneaking = isSneaking
	}
}