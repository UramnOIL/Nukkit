package cn.nukkit.command.defaults

import cn.nukkit.Server.Companion.broadcastPacket
import cn.nukkit.Server.Companion.getDifficultyFromString
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.network.protocol.SetDifficultyPacket
import java.util.*
import kotlin.collections.set

/**
 * Created on 2015/11/12 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class DifficultyCommand(name: String) : VanillaCommand(name, "%nukkit.command.difficulty.description", "%commands.difficulty.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size != 1) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		var difficulty = getDifficultyFromString(args[0]!!)
		if (sender.server.isHardcore) {
			difficulty = 3
		}
		if (difficulty != -1) {
			sender.server.setPropertyInt("difficulty", difficulty)
			val pk = SetDifficultyPacket()
			pk.difficulty = sender.server.getDifficulty()
			broadcastPacket(ArrayList(sender.server.onlinePlayers.values), pk)
			broadcastCommandMessage(sender, TranslationContainer("commands.difficulty.success", difficulty.toString()))
		} else {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		return true
	}

	init {
		permission = "nukkit.command.difficulty"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("difficulty", CommandParamType.INT, false)
		)
		commandParameters["byString"] = arrayOf<CommandParameter?>(
				CommandParameter("difficulty", arrayOf<String?>("peaceful", "p", "easy", "e",
						"normal", "n", "hard", "h"))
		)
	}
}