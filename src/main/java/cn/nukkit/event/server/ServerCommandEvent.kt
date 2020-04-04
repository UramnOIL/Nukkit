package cn.nukkit.event.server

import cn.nukkit.command.CommandSender
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class ServerCommandEvent(val sender: CommandSender, var command: String) : ServerEvent(), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}