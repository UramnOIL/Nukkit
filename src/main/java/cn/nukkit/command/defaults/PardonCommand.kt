package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PardonCommand(name: String) : VanillaCommand(name, "%nukkit.command.unban.player.description", "%commands.unban.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size != 1) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		sender.server.nameBans.remove(args[0])
		broadcastCommandMessage(sender, TranslationContainer("%commands.unban.success", args[0]))
		return true
	}

	init {
		permission = "nukkit.command.unban.player"
		aliases = arrayOf<String?>("unban")
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false)
		)
	}
}