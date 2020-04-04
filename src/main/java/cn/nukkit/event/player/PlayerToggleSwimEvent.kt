package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * @author CreeperFace
 */
class PlayerToggleSwimEvent(player: Player?, isSwimming: Boolean) : PlayerEvent(), Cancellable {
	val isSwimming: Boolean

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.isSwimming = isSwimming
	}
}