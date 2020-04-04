package cn.nukkit.command.defaults

import cn.nukkit.Server.Companion.getGamemodeFromString
import cn.nukkit.Server.Companion.getGamemodeString
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import kotlin.collections.set

/**
 * Created on 2015/11/12 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class DefaultGamemodeCommand(name: String) : VanillaCommand(name, "%nukkit.command.defaultgamemode.description", "%commands.defaultgamemode.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size == 0) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", *arrayOf(usageMessage)))
			return false
		}
		val gameMode = getGamemodeFromString(args[0]!!)
		if (gameMode != -1) {
			sender.server.setPropertyInt("gamemode", gameMode)
			sender.sendMessage(TranslationContainer("commands.defaultgamemode.success", *arrayOf(getGamemodeString(gameMode))))
		} else {
			sender.sendMessage("Unknown game mode") //
		}
		return true
	}

	init {
		permission = "nukkit.command.defaultgamemode"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("mode", CommandParamType.INT, false)
		)
		commandParameters["byString"] = arrayOf<CommandParameter?>(
				CommandParameter("mode", arrayOf<String?>("survival", "creative", "s", "c",
						"adventure", "a", "spectator", "view", "v"))
		)
	}
}