package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class PlayerToggleSprintEvent(player: Player?, isSprinting: Boolean) : PlayerEvent(), Cancellable {
	val isSprinting: Boolean

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.isSprinting = isSprinting
	}
}