package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ReloadCommand(name: String) : VanillaCommand(name, "%nukkit.command.reload.description", "%commands.reload.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		broadcastCommandMessage(sender, TranslationContainer(TextFormat.YELLOW.toString() + "%nukkit.command.reload.reloading" + TextFormat.WHITE))
		sender.server.reload()
		broadcastCommandMessage(sender, TranslationContainer(TextFormat.YELLOW.toString() + "%nukkit.command.reload.reloaded" + TextFormat.WHITE))
		return true
	}

	init {
		permission = "nukkit.command.reload"
		commandParameters.clear()
	}
}