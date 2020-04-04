package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Event
import cn.nukkit.event.HandlerList
import cn.nukkit.network.SourceInterface

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PlayerCreationEvent(val `interface`: SourceInterface, var baseClass: Class<out Player>, var playerClass: Class<out Player>, val clientId: Long, val address: String, val port: Int) : Event() {

	companion object {
		val handlers = HandlerList()
	}

}