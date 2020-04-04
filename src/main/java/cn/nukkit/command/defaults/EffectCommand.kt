package cn.nukkit.command.defaults

import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.potion.Effect
import cn.nukkit.potion.InstantEffect
import cn.nukkit.utils.ServerException
import cn.nukkit.utils.TextFormat
import kotlin.collections.set

/**
 * Created by Snake1999 and Pub4Game on 2016/1/23.
 * Package cn.nukkit.command.defaults in project nukkit.
 */
class EffectCommand(name: String) : Command(name, "%nukkit.command.effect.description", "%commands.effect.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size < 2) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return true
		}
		val player = sender.server.getPlayer(args[0])
		if (player == null) {
			sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.player.notFound"))
			return true
		}
		if (args[1].equals("clear", ignoreCase = true)) {
			for (effect in player.effects.values) {
				player.removeEffect(effect.id)
			}
			sender.sendMessage(TranslationContainer("commands.effect.success.removed.all", player.getDisplayName()))
			return true
		}
		val effect: Effect
		effect = try {
			Effect.getEffect(args[1]!!.toInt())
		} catch (a: NumberFormatException) {
			try {
				Effect.getEffectByName(args[1])
			} catch (e: Exception) {
				sender.sendMessage(TranslationContainer("commands.effect.notFound", args[1]))
				return true
			}
		} catch (a: ServerException) {
			try {
				Effect.getEffectByName(args[1])
			} catch (e: Exception) {
				sender.sendMessage(TranslationContainer("commands.effect.notFound", args[1]))
				return true
			}
		}
		var duration = 300
		var amplification = 0
		if (args.size >= 3) {
			duration = try {
				Integer.valueOf(args[2])
			} catch (a: NumberFormatException) {
				sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
				return true
			}
			if (effect !is InstantEffect) {
				duration *= 20
			}
		} else if (effect is InstantEffect) {
			duration = 1
		}
		if (args.size >= 4) {
			amplification = try {
				Integer.valueOf(args[3])
			} catch (a: NumberFormatException) {
				sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
				return true
			}
		}
		if (args.size >= 5) {
			val v = args[4]!!.toLowerCase()
			if (v.matches("(?i)|on|true|t|1")) {
				effect.isVisible = false
			}
		}
		if (duration == 0) {
			if (!player.hasEffect(effect.id)) {
				if (player.effects.size == 0) {
					sender.sendMessage(TranslationContainer("commands.effect.failure.notActive.all", player.getDisplayName()))
				} else {
					sender.sendMessage(TranslationContainer("commands.effect.failure.notActive", effect.name, player.getDisplayName()))
				}
				return true
			}
			player.removeEffect(effect.id)
			sender.sendMessage(TranslationContainer("commands.effect.success.removed", effect.name, player.getDisplayName()))
		} else {
			effect.setDuration(duration).amplifier = amplification
			player.addEffect(effect)
			broadcastCommandMessage(sender, TranslationContainer("%commands.effect.success", effect.name, effect.amplifier.toString(), player.getDisplayName(), (effect.duration / 20).toString()))
		}
		return true
	}

	init {
		permission = "nukkit.command.effect"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("effect", CommandParamType.STRING, false),  //Do not use Enum here because of buggy behavior
				CommandParameter("seconds", CommandParamType.INT, true),
				CommandParameter("amplifier", true),
				CommandParameter("hideParticle", true, arrayOf<String?>("true", "false"))
		)
		commandParameters["clear"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, false),
				CommandParameter("clear", arrayOf<String?>("clear"))
		)
	}
}