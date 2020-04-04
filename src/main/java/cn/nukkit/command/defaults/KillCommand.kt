package cn.nukkit.command.defaults

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import java.util.*
import kotlin.collections.set

/**
 * Created on 2015/12/08 by Pub4Game.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class KillCommand(name: String) : VanillaCommand(name, "%nukkit.command.kill.description", "%nukkit.command.kill.usage", arrayOf<String?>("suicide")) {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size >= 2) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		if (args.size == 1) {
			if (!sender.hasPermission("nukkit.command.kill.other")) {
				sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.permission"))
				return true
			}
			val player = sender.server.getPlayer(args[0])
			if (player != null) {
				val ev = EntityDamageEvent(player, DamageCause.SUICIDE, 1000)
				sender.server.pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					return true
				}
				player.lastDamageCause = ev
				player.health = 0f
				broadcastCommandMessage(sender, TranslationContainer("commands.kill.successful", player.getName()))
			} else if (args[0] == "@e") {
				val joiner = StringJoiner(", ")
				for (level in Server.instance!!.getLevels().values) {
					for (entity in level.entities) {
						if (entity !is Player) {
							joiner.add(entity.name)
							entity.close()
						}
					}
				}
				val entities = joiner.toString()
				sender.sendMessage(TranslationContainer("commands.kill.successful", if (entities.isEmpty()) "0" else entities))
			} else if (args[0] == "@s") {
				if (!sender.hasPermission("nukkit.command.kill.self")) {
					sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.permission"))
					return true
				}
				val ev = EntityDamageEvent(sender as Player, DamageCause.SUICIDE, 1000)
				sender.server.pluginManager.callEvent(ev)
				if (ev.isCancelled) {
					return true
				}
				(sender as Player).lastDamageCause = ev
				sender.health = 0f
				sender.sendMessage(TranslationContainer("commands.kill.successful", sender.getName()))
			} else if (args[0] == "@a") {
				if (!sender.hasPermission("nukkit.command.kill.other")) {
					sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.permission"))
					return true
				}
				for (level in Server.instance!!.getLevels().values) {
					for (entity in level.entities) {
						(entity as? Player)?.health = 0f
					}
				}
				sender.sendMessage(TranslationContainer(TextFormat.GOLD.toString() + "%commands.kill.all.successful"))
			} else {
				sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.player.notFound"))
			}
			return true
		}
		if (sender is Player) {
			if (!sender.hasPermission("nukkit.command.kill.self")) {
				sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.permission"))
				return true
			}
			val ev = EntityDamageEvent(sender, DamageCause.SUICIDE, 1000)
			sender.getServer().pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				return true
			}
			sender.lastDamageCause = ev
			sender.health = 0f
			sender.sendMessage(TranslationContainer("commands.kill.successful", sender.getName()))
		} else {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		return true
	}

	init {
		permission = ("nukkit.command.kill.self;"
				+ "nukkit.command.kill.other")
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("player", CommandParamType.TARGET, true)
		)
	}
}