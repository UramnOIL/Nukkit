package cn.nukkit.command.defaults

import cn.nukkit.Server
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import kotlin.collections.set

/**
 * @author Tee7even
 */
class TitleCommand(name: String) : VanillaCommand(name, "%nukkit.command.title.description", "%nukkit.command.title.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size < 2) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		val player = Server.instance!!.getPlayerExact(args[0]!!)
		if (player == null) {
			sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.player.notFound"))
			return true
		}
		if (args.size == 2) {
			when (args[1]!!.toLowerCase()) {
				"clear" -> {
					player.clearTitle()
					sender.sendMessage(TranslationContainer("nukkit.command.title.clear", player.getName()))
				}
				"reset" -> {
					player.resetTitleSettings()
					sender.sendMessage(TranslationContainer("nukkit.command.title.reset", player.getName()))
				}
				else -> {
					sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
					return false
				}
			}
		} else if (args.size == 3) {
			when (args[1]!!.toLowerCase()) {
				"title" -> {
					player.sendTitle(args[2])
					sender.sendMessage(TranslationContainer("nukkit.command.title.title",
							TextFormat.clean(args[2]), player.getName()))
				}
				"subtitle" -> {
					player.setSubtitle(args[2])
					sender.sendMessage(TranslationContainer("nukkit.command.title.subtitle", TextFormat.clean(args[2]), player.getName()))
				}
				else -> {
					sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
					return false
				}
			}
		} else if (args.size == 5) {
			if (args[1]!!.toLowerCase() == "times") {
				try {
					/*player.setTitleAnimationTimes(Integer.valueOf(args[2]), //fadeIn
                            Integer.valueOf(args[3]), //stay
                            Integer.valueOf(args[4])); //fadeOut*/
					sender.sendMessage(TranslationContainer("nukkit.command.title.times.success",
							args[2], args[3], args[4], player.getName()))
				} catch (exception: NumberFormatException) {
					sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%nukkit.command.title.times.fail"))
				}
			} else {
				sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
				return false
			}
		} else {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		return true
	}

	init {
		permission = "nukkit.command.title"
		commandParameters.clear()
		commandParameters["clear"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("clear", arrayOf<String?>("clear"))
		)
		commandParameters["reset"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("reset", arrayOf<String?>("reset"))
		)
		commandParameters["title"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("title", arrayOf<String?>("title")),
				CommandParameter("titleText", CommandParamType.STRING, false)
		)
		commandParameters["subtitle"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("subtitle", arrayOf<String?>("subtitle")),
				CommandParameter("titleText", CommandParamType.STRING, false)
		)
		commandParameters["actionbar"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("actionbar", arrayOf<String?>("actionbar")),
				CommandParameter("titleText", CommandParamType.STRING, false)
		)
		commandParameters["times"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("times", arrayOf<String?>("times")),
				CommandParameter("fadeIn", CommandParamType.INT, false),
				CommandParameter("stay", CommandParamType.INT, false),
				CommandParameter("fadeOut", CommandParamType.INT, false)
		)
	}
}