package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.lang.TranslationContainer

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class SeedCommand(name: String) : VanillaCommand(name, "%nukkit.command.seed.description", "%commands.seed.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		val seed: Long
		seed = if (sender is Player) {
			sender.level.seed
		} else {
			sender.server.defaultLevel.getSeed()
		}
		sender.sendMessage(TranslationContainer("commands.seed.success", seed.toString()))
		return true
	}

	init {
		permission = "nukkit.command.seed"
		commandParameters.clear()
	}
}