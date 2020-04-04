package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Event

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class PlayerEvent : Event() {
	var player: Player? = null
		protected set
}