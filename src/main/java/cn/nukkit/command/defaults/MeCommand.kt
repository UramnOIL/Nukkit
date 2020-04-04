package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import kotlin.collections.set

/**
 * Created on 2015/11/12 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class MeCommand(name: String) : VanillaCommand(name, "%nukkit.command.me.description", "%commands.me.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size == 0) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		val name: String?
		name = if (sender is Player) {
			sender.getDisplayName()
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
		sender.server.broadcastMessage(TranslationContainer("chat.type.emote", name, TextFormat.WHITE.toString() + msg))
		return true
	}

	init {
		permission = "nukkit.command.me"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("action ...", CommandParamType.RAWTEXT, false)
		)
	}
}