package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.network.protocol.AnimatePacket

class PlayerAnimationEvent @JvmOverloads constructor(player: Player?, animation: AnimatePacket.Action = AnimatePacket.Action.SWING_ARM) : PlayerEvent(), Cancellable {
	val animationType: AnimatePacket.Action

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		animationType = animation
	}
}