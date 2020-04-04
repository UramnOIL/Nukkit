package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.lang.TranslationContainer

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class StopCommand(name: String) : VanillaCommand(name, "%nukkit.command.stop.description", "%commands.stop.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		broadcastCommandMessage(sender, TranslationContainer("commands.stop.start"))
		sender.server.shutdown()
		return true
	}

	init {
		permission = "nukkit.command.stop"
		commandParameters.clear()
	}
}