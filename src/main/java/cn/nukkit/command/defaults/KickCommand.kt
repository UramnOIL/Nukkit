package cn.nukkit.command.defaults

import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.event.player.PlayerKickEvent
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import kotlin.collections.set

/**
 * Created on 2015/11/11 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class KickCommand(name: String?) : VanillaCommand(name, "%nukkit.command.kick.description", "%commands.kick.usage") {
	override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size == 0) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		val name = args[0]
		var reason = ""
		for (i in 1 until args.size) {
			reason += args[i] + " "
		}
		if (reason.length > 0) {
			reason = reason.substring(0, reason.length - 1)
		}
		val player = sender.server.getPlayer(name)
		if (player != null) {
			player.kick(PlayerKickEvent.Reason.KICKED_BY_ADMIN, reason)
			if (reason.length >= 1) {
				Command.broadcastCommandMessage(sender, TranslationContainer("commands.kick.success.reason", player.getName(), reason)
				)
			} else {
				Command.broadcastCommandMessage(sender, TranslationContainer("commands.kick.success", player.getName()))
			}
		} else {
			sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.player.notFound"))
		}
		return true
	}

	init {
		permission = "nukkit.command.kick"
		commandParameters.clear()
		commandParameters["default"] = arrayOf(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("reason", true)
		)
	}
}