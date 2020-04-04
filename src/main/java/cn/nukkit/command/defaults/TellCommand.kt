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
class TellCommand(name: String) : VanillaCommand(name, "%nukkit.command.tell.description", "%commands.message.usage", arrayOf<String?>("w", "msg")) {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size < 2) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		val name = args[0]!!.toLowerCase()
		val player = sender.server.getPlayer(name)
		if (player == null) {
			sender.sendMessage(TranslationContainer("commands.generic.player.notFound"))
			return true
		}
		if (player == sender) {
			sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.message.sameTarget"))
			return true
		}
		var msg = ""
		for (i in 1 until args.size) {
			msg += args[i].toString() + " "
		}
		if (msg.length > 0) {
			msg = msg.substring(0, msg.length - 1)
		}
		val displayName = if (sender is Player) sender.getDisplayName() else sender.name
		sender.sendMessage("[" + sender.name + " -> " + player.getDisplayName() + "] " + msg)
		player.sendMessage("[" + displayName + " -> " + player.getName() + "] " + msg)
		return true
	}

	init {
		permission = "nukkit.command.tell"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("message")
		)
	}
}