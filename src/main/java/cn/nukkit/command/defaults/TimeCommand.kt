package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.level.Level
import cn.nukkit.utils.TextFormat
import kotlin.collections.set

/**
 * Created on 2015/11/11 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class TimeCommand(name: String) : VanillaCommand(name, "%nukkit.command.time.description", "%nukkit.command.time.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (args.size < 1) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		if ("start" == args[0]) {
			if (!sender.hasPermission("nukkit.command.time.start")) {
				sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.permission"))
				return true
			}
			for (level in sender.server.getLevels().values) {
				level.checkTime()
				level.startTime()
				level.checkTime()
			}
			broadcastCommandMessage(sender, "Restarted the time")
			return true
		} else if ("stop" == args[0]) {
			if (!sender.hasPermission("nukkit.command.time.stop")) {
				sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.permission"))
				return true
			}
			for (level in sender.server.getLevels().values) {
				level.checkTime()
				level.stopTime()
				level.checkTime()
			}
			broadcastCommandMessage(sender, "Stopped the time")
			return true
		} else if ("query" == args[0]) {
			if (!sender.hasPermission("nukkit.command.time.query")) {
				sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.permission"))
				return true
			}
			val level: Level?
			level = if (sender is Player) {
				sender.level
			} else {
				sender.server.defaultLevel
			}
			sender.sendMessage(TranslationContainer("commands.time.query.gametime", level!!.time.toString()))
			return true
		}
		if (args.size < 2) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		if ("set" == args[0]) {
			if (!sender.hasPermission("nukkit.command.time.set")) {
				sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.permission"))
				return true
			}
			val value: Int
			value = if ("day" == args[1]) {
				Level.TIME_DAY
			} else if ("night" == args[1]) {
				Level.TIME_NIGHT
			} else if ("midnight" == args[1]) {
				Level.TIME_MIDNIGHT
			} else if ("noon" == args[1]) {
				Level.TIME_NOON
			} else if ("sunrise" == args[1]) {
				Level.TIME_SUNRISE
			} else if ("sunset" == args[1]) {
				Level.TIME_SUNSET
			} else {
				try {
					Math.max(0, args[1]!!.toInt())
				} catch (e: Exception) {
					sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
					return true
				}
			}
			for (level in sender.server.getLevels().values) {
				level.checkTime()
				level.time = value
				level.checkTime()
			}
			broadcastCommandMessage(sender, TranslationContainer("commands.time.set", value.toString()))
		} else if ("add" == args[0]) {
			if (!sender.hasPermission("nukkit.command.time.add")) {
				sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.permission"))
				return true
			}
			val value: Int
			value = try {
				Math.max(0, args[1]!!.toInt())
			} catch (e: Exception) {
				sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
				return true
			}
			for (level in sender.server.getLevels().values) {
				level.checkTime()
				level.time = level.time + value
				level.checkTime()
			}
			broadcastCommandMessage(sender, TranslationContainer("commands.time.added", value.toString()))
		} else {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
		}
		return true
	}

	init {
		permission = "nukkit.command.time.add;" +
				"nukkit.command.time.set;" +
				"nukkit.command.time.start;" +
				"nukkit.command.time.stop"
		commandParameters.clear()
		commandParameters["1arg"] = arrayOf<CommandParameter?>(
				CommandParameter("start|stop", CommandParamType.STRING, false)
		)
		commandParameters["2args"] = arrayOf<CommandParameter?>(
				CommandParameter("add|set", CommandParamType.STRING, false),
				CommandParameter("value", CommandParamType.INT, false)
		)
		commandParameters["2args_"] = arrayOf<CommandParameter?>(
				CommandParameter("add|set", CommandParamType.STRING, false),
				CommandParameter("value", CommandParamType.STRING, false)
		)
	}
}