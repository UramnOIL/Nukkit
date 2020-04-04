package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.lang.TranslationContainer

/**
 * Created on 2015/11/13 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class SaveOffCommand(name: String) : VanillaCommand(name, "%nukkit.command.saveoff.description", "%commands.save-off.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		sender.server.setAutoSave(false)
		broadcastCommandMessage(sender, TranslationContainer("commands.save.disabled"))
		return true
	}

	init {
		permission = "nukkit.command.save.disable"
		commandParameters.clear()
	}
}