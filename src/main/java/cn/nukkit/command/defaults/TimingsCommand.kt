package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import co.aikar.timings.Timings
import co.aikar.timings.TimingsExport
import kotlin.collections.set

/**
 * @author fromgate
 * @author Pub4Game
 */
class TimingsCommand(name: String) : VanillaCommand(name, "%nukkit.command.timings.description", "%nukkit.command.timings.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size != 1) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return true
		}
		val mode = args[0]!!.toLowerCase()
		if (mode == "on") {
			Timings.setTimingsEnabled(true)
			Timings.reset()
			sender.sendMessage(TranslationContainer("nukkit.command.timings.enable"))
			return true
		} else if (mode == "off") {
			Timings.setTimingsEnabled(false)
			sender.sendMessage(TranslationContainer("nukkit.command.timings.disable"))
			return true
		}
		if (!Timings.isTimingsEnabled()) {
			sender.sendMessage(TranslationContainer("nukkit.command.timings.timingsDisabled"))
			return true
		}
		when (mode) {
			"verbon" -> {
				sender.sendMessage(TranslationContainer("nukkit.command.timings.verboseEnable"))
				Timings.setVerboseEnabled(true)
			}
			"verboff" -> {
				sender.sendMessage(TranslationContainer("nukkit.command.timings.verboseDisable"))
				Timings.setVerboseEnabled(true)
			}
			"reset" -> {
				Timings.reset()
				sender.sendMessage(TranslationContainer("nukkit.command.timings.reset"))
			}
			"report", "paste" -> TimingsExport.reportTimings(sender)
		}
		return true
	}

	init {
		permission = "nukkit.command.timings"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("on|off|paste")
		)
	}
}