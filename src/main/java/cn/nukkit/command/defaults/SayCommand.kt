package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.ConsoleCommandSender
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import kotlin.collections.set

/**
 * Created on 2015/11/12 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class SayCommand(name: String) : VanillaCommand(name, "%nukkit.command.say.description", "%commands.say.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size == 0) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		val senderString: String?
		senderString = if (sender is Player) {
			sender.getDisplayName()
		} else if (sender is ConsoleCommandSender) {
			"Server"
		} else {
			sender.name
		}
		var msg = ""
		for (arg in args) {
			msg += "$arg "
		}
		if (msg.length > 0) {
			msg = msg.substring(0, msg.length - 1)
		}
		sender.server.broadcastMessage(TranslationContainer(TextFormat.LIGHT_PURPLE.toString() + "%chat.type.announcement",
				senderString, TextFormat.LIGHT_PURPLE.toString() + msg))
		return true
	}

	init {
		permission = "nukkit.command.say"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("message")
		)
	}
}