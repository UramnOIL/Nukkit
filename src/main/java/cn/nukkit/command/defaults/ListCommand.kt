package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.lang.TranslationContainer

/**
 * Created on 2015/11/11 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class ListCommand(name: String) : VanillaCommand(name, "%nukkit.command.list.description", "%commands.players.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		var online = ""
		var onlineCount = 0
		for (player in sender.server.onlinePlayers.values) {
			if (player.isOnline() && (sender !is Player || sender.canSee(player))) {
				online += player.getDisplayName() + ", "
				++onlineCount
			}
		}
		if (online.length > 0) {
			online = online.substring(0, online.length - 2)
		}
		sender.sendMessage(TranslationContainer("commands.players.list", onlineCount.toString(), sender.server.maxPlayers.toString()))
		sender.sendMessage(online)
		return true
	}

	init {
		permission = "nukkit.command.list"
		commandParameters.clear()
	}
}