package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.level.Position
import cn.nukkit.utils.TextFormat
import java.text.DecimalFormat
import kotlin.collections.set

/**
 * Created on 2015/12/13 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class SpawnpointCommand(name: String) : VanillaCommand(name, "%nukkit.command.spawnpoint.description", "%commands.spawnpoint.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		val target: Player?
		if (args.size == 0) {
			target = if (sender is Player) {
				sender
			} else {
				sender.sendMessage(TranslationContainer("commands.generic.ingame"))
				return true
			}
		} else {
			target = sender.server.getPlayer(args[0])
			if (target == null) {
				sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.player.notFound"))
				return true
			}
		}
		val level = target!!.level
		val round2 = DecimalFormat("##0.00")
		if (args.size == 4) {
			if (level != null) {
				val x: Int
				var y: Int
				val z: Int
				try {
					x = args[1]!!.toInt()
					y = args[2]!!.toInt()
					z = args[3]!!.toInt()
				} catch (e1: NumberFormatException) {
					sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
					return true
				}
				if (y < 0) y = 0
				if (y > 256) y = 256
				target.setSpawn(Position(x.toDouble(), y.toDouble(), z.toDouble(), level))
				broadcastCommandMessage(sender, TranslationContainer("commands.spawnpoint.success", target.getName(),
						round2.format(x.toLong()),
						round2.format(y.toLong()),
						round2.format(z.toLong())))
				return true
			}
		} else if (args.size <= 1) {
			return if (sender is Player) {
				val pos = sender as Position
				target.setSpawn(pos)
				broadcastCommandMessage(sender, TranslationContainer("commands.spawnpoint.success", target.getName(),
						round2.format(pos.x),
						round2.format(pos.y),
						round2.format(pos.z)))
				true
			} else {
				sender.sendMessage(TranslationContainer("commands.generic.ingame"))
				true
			}
		}
		sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
		return true
	}

	init {
		permission = "nukkit.command.spawnpoint"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("blockPos", CommandParamType.POSITION, true))
	}
}