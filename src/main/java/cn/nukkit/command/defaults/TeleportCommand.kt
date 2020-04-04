package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.event.player.PlayerTeleportEvent
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.level.Location
import cn.nukkit.math.NukkitMath
import cn.nukkit.utils.TextFormat
import kotlin.collections.set

/**
 * Created on 2015/11/12 by Pub4Game and milkice.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class TeleportCommand(name: String) : VanillaCommand(name, "%nukkit.command.tp.description", "%commands.tp.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size < 1 || args.size > 6) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return true
		}
		var target: CommandSender?
		var origin = sender
		if (args.size == 1 || args.size == 3) {
			target = if (sender is Player) {
				sender
			} else {
				sender.sendMessage(TranslationContainer("commands.generic.ingame"))
				return true
			}
			if (args.size == 1) {
				target = sender.getServer().getPlayer(args[0]!!.replace("@s", sender.getName()))
				if (target == null) {
					sender.sendMessage(TextFormat.RED.toString() + "Can't find player " + args[0])
					return true
				}
			}
		} else {
			target = sender.server.getPlayer(args[0]!!.replace("@s", sender.name))
			if (target == null) {
				sender.sendMessage(TextFormat.RED.toString() + "Can't find player " + args[0])
				return true
			}
			if (args.size == 2) {
				origin = target
				target = sender.server.getPlayer(args[1]!!.replace("@s", sender.name))
				if (target == null) {
					sender.sendMessage(TextFormat.RED.toString() + "Can't find player " + args[1])
					return true
				}
			}
		}
		if (args.size < 3) {
			(origin as Player).teleport(target as Player?, PlayerTeleportEvent.TeleportCause.COMMAND)
			broadcastCommandMessage(sender, TranslationContainer("commands.tp.success", origin.getName(), target.getName()))
			return true
		} else if ((target as Player).level != null) {
			var pos: Int
			pos = if (args.size == 4 || args.size == 6) {
				1
			} else {
				0
			}
			val x: Double
			var y: Double
			val z: Double
			var yaw: Double
			var pitch: Double
			try {
				x = args[pos++]!!.replace("~", "" + target.x).toDouble()
				y = args[pos++]!!.replace("~", "" + target.y).toDouble()
				z = args[pos++]!!.replace("~", "" + target.z).toDouble()
				yaw = target.getYaw()
				pitch = target.getPitch()
			} catch (e1: NumberFormatException) {
				sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
				return true
			}
			if (y < 0) y = 0.0
			if (y > 256) y = 256.0
			if (args.size == 6 || args.size == 5 && pos == 3) {
				yaw = args[pos++]!!.toInt().toDouble()
				pitch = args[pos++]!!.toInt().toDouble()
			}
			target.teleport(Location(x, y, z, yaw, pitch, target.level), PlayerTeleportEvent.TeleportCause.COMMAND)
			broadcastCommandMessage(sender, TranslationContainer("commands.tp.success.coordinates", target.getName(), NukkitMath.round(x, 2).toString(), NukkitMath.round(y, 2).toString(), NukkitMath.round(z, 2).toString()))
			return true
		}
		sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
		return true
	}

	init {
		permission = "nukkit.command.teleport"
		commandParameters.clear()
		commandParameters["->Player"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false))
		commandParameters["Player->Player"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("target", CommandParamType.TARGET, false))
		commandParameters["Player->Pos"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("blockPos", CommandParamType.POSITION, false))
		commandParameters["->Pos"] = arrayOf<CommandParameter?>(
				CommandParameter("blockPos", CommandParamType.POSITION, false))
	}
}