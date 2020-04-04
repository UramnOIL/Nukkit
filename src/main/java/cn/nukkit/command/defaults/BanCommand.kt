package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.event.player.PlayerKickEvent
import cn.nukkit.lang.TranslationContainer
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BanCommand(name: String) : VanillaCommand(name, "%nukkit.command.ban.player.description", "%commands.ban.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
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
			reason += args[i].toString() + " "
		}
		if (reason.length > 0) {
			reason = reason.substring(0, reason.length - 1)
		}
		sender.server.nameBans.addBan(name, reason, null, sender.name)
		val player = sender.server.getPlayerExact(name)
		player?.kick(PlayerKickEvent.Reason.NAME_BANNED, if (!reason.isEmpty()) "Banned by admin. Reason: $reason" else "Banned by admin")
		broadcastCommandMessage(sender, TranslationContainer("%commands.ban.success", player?.getName() ?: name))
		return true
	}

	init {
		permission = "nukkit.command.ban.player"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("reason", CommandParamType.STRING, true)
		)
	}
}