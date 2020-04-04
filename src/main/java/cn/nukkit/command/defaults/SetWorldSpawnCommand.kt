package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.level.Level
import cn.nukkit.math.Vector3
import java.text.DecimalFormat
import kotlin.collections.set

/**
 * Created on 2015/12/13 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class SetWorldSpawnCommand(name: String) : VanillaCommand(name, "%nukkit.command.setworldspawn.description", "%commands.setworldspawn.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		val level: Level?
		val pos: Vector3
		if (args.size == 0) {
			if (sender is Player) {
				level = sender.level
				pos = sender.round()
			} else {
				sender.sendMessage(TranslationContainer("commands.generic.ingame"))
				return true
			}
		} else if (args.size == 3) {
			level = sender.server.defaultLevel
			pos = try {
				Vector3(args[0]!!.toInt().toDouble(), args[1]!!.toInt().toDouble(), args[2]!!.toInt().toDouble())
			} catch (e1: NumberFormatException) {
				sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
				return true
			}
		} else {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return true
		}
		level!!.setSpawnLocation(pos)
		val round2 = DecimalFormat("##0.00")
		broadcastCommandMessage(sender, TranslationContainer("commands.setworldspawn.success", round2.format(pos.x),
				round2.format(pos.y),
				round2.format(pos.z)))
		return true
	}

	init {
		permission = "nukkit.command.setworldspawn"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("blockPos", CommandParamType.POSITION, true)
		)
	}
}