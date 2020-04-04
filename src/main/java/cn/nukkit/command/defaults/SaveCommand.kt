package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.lang.TranslationContainer

/**
 * Created on 2015/11/13 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class SaveCommand(name: String) : VanillaCommand(name, "%nukkit.command.save.description", "%commands.save.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		broadcastCommandMessage(sender, TranslationContainer("commands.save.start"))
		for (player in sender.server.onlinePlayers.values) {
			player.save()
		}
		for (level in sender.server.getLevels().values) {
			level.save(true)
		}
		broadcastCommandMessage(sender, TranslationContainer("commands.save.success"))
		return true
	}

	init {
		permission = "nukkit.command.save.perform"
		commandParameters.clear()
	}
}