package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.Server.Companion.getGamemodeFromString
import cn.nukkit.Server.Companion.getGamemodeString
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import kotlin.collections.set

/**
 * Created on 2015/11/13 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class GamemodeCommand(name: String) : VanillaCommand(name, "%nukkit.command.gamemode.description", "%commands.gamemode.usage", arrayOf<String?>("gm")) {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (args.size == 0) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		val gameMode = getGamemodeFromString(args[0]!!)
		if (gameMode == -1) {
			sender.sendMessage("Unknown game mode")
			return true
		}
		var target: CommandSender? = sender
		if (args.size > 1) {
			if (sender.hasPermission("nukkit.command.gamemode.other")) {
				target = sender.server.getPlayer(args[1])
				if (target == null) {
					sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.player.notFound"))
					return true
				}
			} else {
				sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.permission"))
				return true
			}
		} else if (sender !is Player) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return true
		}
		if (gameMode == 0 && !sender.hasPermission("nukkit.command.gamemode.survival") ||
				gameMode == 1 && !sender.hasPermission("nukkit.command.gamemode.creative") ||
				gameMode == 2 && !sender.hasPermission("nukkit.command.gamemode.adventure") ||
				gameMode == 3 && !sender.hasPermission("nukkit.command.gamemode.spectator")) {
			sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.permission"))
			return true
		}
		if (!(target as Player?)!!.setGamemode(gameMode)) {
			sender.sendMessage("Game mode update for " + target!!.getName() + " failed")
		} else {
			if (target == sender) {
				broadcastCommandMessage(sender, TranslationContainer("commands.gamemode.success.self", getGamemodeString(gameMode)))
			} else {
				target!!.sendMessage(TranslationContainer("gameMode.changed"))
				broadcastCommandMessage(sender, TranslationContainer("commands.gamemode.success.other", target.getName(), getGamemodeString(gameMode)))
			}
		}
		return true
	}

	init {
		permission = "nukkit.command.gamemode.survival;" +
				"nukkit.command.gamemode.creative;" +
				"nukkit.command.gamemode.adventure;" +
				"nukkit.command.gamemode.spectator;" +
				"nukkit.command.gamemode.other"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("mode", CommandParamType.INT, false),
				CommandParameter("player", CommandParamType.TARGET, true)
		)
		commandParameters["byString"] = arrayOf<CommandParameter?>(
				CommandParameter("mode", arrayOf<String?>("survival", "s", "creative", "c",
						"adventure", "a", "spectator", "spc", "view", "v")),
				CommandParameter("player", CommandParamType.TARGET, true)
		)
	}
}