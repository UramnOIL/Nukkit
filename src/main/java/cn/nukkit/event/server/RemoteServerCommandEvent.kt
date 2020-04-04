package cn.nukkit.event.server

import cn.nukkit.command.CommandSender
import cn.nukkit.event.HandlerList

/**
 * Called when an RCON command is executed.
 *
 * @author Tee7even
 */
class RemoteServerCommandEvent(sender: CommandSender, command: String) : ServerCommandEvent(sender, command) {
	companion object {
		val handlers = HandlerList()
	}
}