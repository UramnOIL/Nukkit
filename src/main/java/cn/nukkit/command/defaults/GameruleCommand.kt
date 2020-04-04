package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.level.GameRule
import java.util.*
import kotlin.collections.set

class GameruleCommand(name: String) : VanillaCommand(name, "%nukkit.command.gamerule.description", "%nukkit.command.gamerule.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (!sender.isPlayer) {
			sender.sendMessage(TranslationContainer("%commands.generic.ingame"))
			return true
		}
		val rules = (sender as Player).level.getGameRules()
		return when (args.size) {
			0 -> {
				val rulesJoiner = StringJoiner(", ")
				for (rule in rules.rules) {
					rulesJoiner.add(rule.getName().toLowerCase())
				}
				sender.sendMessage(rulesJoiner.toString())
				true
			}
			1 -> {
				val gameRule = GameRule.parseString(args[0])
				if (!gameRule.isPresent || !rules.hasRule(gameRule.get())) {
					sender.sendMessage(TranslationContainer("commands.generic.syntax", "/gamerule", args[0]))
					return true
				}
				sender.sendMessage(gameRule.get().name + " = " + rules.getString(gameRule.get()))
				true
			}
			else -> {
				val optionalRule = GameRule.parseString(args[0])
				if (!optionalRule.isPresent) {
					sender.sendMessage(TranslationContainer("commands.generic.syntax",
							"/gamerule ", args[0], " " + java.lang.String.join(" ", *Arrays.copyOfRange(args, 1, args.size))))
					return true
				}
				try {
					rules.setGameRules(optionalRule.get(), args[1])
					sender.sendMessage(TranslationContainer("commands.gamerule.success", optionalRule.get().name, args[1]))
				} catch (e: IllegalArgumentException) {
					sender.sendMessage(TranslationContainer("commands.generic.syntax", "/gamerule " + args[0] + " ", args[1], " " + java.lang.String.join(" ", *Arrays.copyOfRange(args, 2, args.size))))
				}
				true
			}
		}
	}

	init {
		permission = "nukkit.command.gamerule"
		commandParameters.clear()
		commandParameters["byString"] = arrayOf<CommandParameter?>(
				CommandParameter("gamerule", true, GameRule.names),
				CommandParameter("value", CommandParamType.STRING, true)
		)
	}
}