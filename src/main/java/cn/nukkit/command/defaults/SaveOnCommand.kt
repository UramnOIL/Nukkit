package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.lang.TranslationContainer

/**
 * Created on 2015/11/13 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class SaveOnCommand(name: String) : VanillaCommand(name, "%nukkit.command.saveon.description", "%commands.save-on.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		sender.server.setAutoSave(true)
		broadcastCommandMessage(sender, TranslationContainer("commands.save.enabled"))
		return true
	}

	init {
		permission = "nukkit.command.save.enable"
		commandParameters.clear()
	}
}