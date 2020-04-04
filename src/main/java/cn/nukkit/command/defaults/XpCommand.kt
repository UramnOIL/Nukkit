package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import kotlin.collections.set

/**
 * Created by Snake1999 on 2016/1/22.
 * Package cn.nukkit.command.defaults in project nukkit.
 */
class XpCommand(name: String) : Command(name, "%nukkit.command.xp.description", "%commands.xp.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}

		//  "/xp <amount> [player]"  for adding exp
		//  "/xp <amount>L [player]" for adding exp level
		var amountString: String?
		val playerName: String?
		val player: Player?
		if (sender !is Player) {
			if (args.size != 2) {
				sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
				return true
			}
			amountString = args[0]
			playerName = args[1]
			player = sender.server.getPlayer(playerName)
		} else {
			if (args.size == 1) {
				amountString = args[0]
				player = sender
			} else if (args.size == 2) {
				amountString = args[0]
				playerName = args[1]
				player = sender.getServer().getPlayer(playerName!!)
			} else {
				sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
				return true
			}
		}
		if (player == null) {
			sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.player.notFound"))
			return true
		}
		val amount: Int
		var isLevel = false
		if (amountString!!.endsWith("l") || amountString.endsWith("L")) {
			amountString = amountString.substring(0, amountString.length - 1)
			isLevel = true
		}
		amount = try {
			amountString.toInt()
		} catch (e1: NumberFormatException) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return true
		}
		return if (isLevel) {
			var newLevel = player.experienceLevel
			newLevel += amount
			if (newLevel > 24791) newLevel = 24791
			if (newLevel < 0) {
				player.setExperience(0, 0)
			} else {
				player.setExperience(player.experience, newLevel)
			}
			if (amount > 0) {
				sender.sendMessage(TranslationContainer("commands.xp.success.levels", amount.toString(), player.getName()))
			} else {
				sender.sendMessage(TranslationContainer("commands.xp.success.levels.minus", -amount.toString(), player.getName()))
			}
			true
		} else {
			if (amount < 0) {
				sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
				return true
			}
			player.addExperience(amount)
			sender.sendMessage(TranslationContainer("commands.xp.success", amount.toString(), player.getName()))
			true
		}
	}

	init {
		permission = "nukkit.command.xp"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("amount|level", CommandParamType.INT, false),
				CommandParameter("player", CommandParamType.TARGET, true)
		)
	}
}