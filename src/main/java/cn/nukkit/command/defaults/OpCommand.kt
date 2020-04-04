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
class OpCommand(name: String) : VanillaCommand(name, "%nukkit.command.op.description", "%commands.op.description") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size == 0) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		val name = args[0]
		val player = sender.server.getOfflinePlayer(name)
		broadcastCommandMessage(sender, TranslationContainer("commands.op.success", player.name))
		if (player is Player) {
			player.sendMessage(TranslationContainer(TextFormat.GRAY.toString() + "%commands.op.message"))
		}
		player.isOp = true
		return true
	}

	init {
		permission = "nukkit.command.op.give"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false)
		)
	}
}