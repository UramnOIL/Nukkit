package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.level.Level
import java.util.*
import kotlin.collections.set

/**
 * author: Angelic47
 * Nukkit Project
 */
class WeatherCommand(name: String) : VanillaCommand(name, "%nukkit.command.weather.description", "%commands.weather.usage") {
	private val rand = Random()
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size == 0 || args.size > 2) {
			sender.sendMessage(TranslationContainer("commands.weather.usage", usageMessage))
			return false
		}
		val weather = args[0]
		val level: Level?
		val seconds: Int
		seconds = if (args.size > 1) {
			try {
				args[1]!!.toInt()
			} catch (e: Exception) {
				sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
				return true
			}
		} else {
			600 * 20
		}
		level = if (sender is Player) {
			sender.level
		} else {
			sender.server.defaultLevel
		}
		return when (weather) {
			"clear" -> {
				level!!.isRaining = false
				level.isThundering = false
				level.rainTime = seconds * 20
				level.thunderTime = seconds * 20
				broadcastCommandMessage(sender,
						TranslationContainer("commands.weather.clear"))
				true
			}
			"rain" -> {
				level!!.isRaining = true
				level.rainTime = seconds * 20
				broadcastCommandMessage(sender,
						TranslationContainer("commands.weather.rain"))
				true
			}
			"thunder" -> {
				level!!.isThundering = true
				level.rainTime = seconds * 20
				level.thunderTime = seconds * 20
				broadcastCommandMessage(sender,
						TranslationContainer("commands.weather.thunder"))
				true
			}
			else -> {
				sender.sendMessage(TranslationContainer("commands.weather.usage", usageMessage))
				false
			}
		}
	}

	init {
		permission = "nukkit.command.weather"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("clear|rain|thunder", CommandParamType.STRING, false),
				CommandParameter("duration in seconds", CommandParamType.INT, true)
		)
	}
}