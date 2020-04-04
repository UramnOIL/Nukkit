package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import kotlin.collections.set

/**
 * Created on 2015/11/12 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class WhitelistCommand(name: String) : VanillaCommand(name, "%nukkit.command.whitelist.description", "%commands.whitelist.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size == 0 || args.size > 2) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return true
		}
		if (args.size == 1) {
			if (badPerm(sender, args[0]!!.toLowerCase())) {
				return false
			}
			when (args[0]!!.toLowerCase()) {
				"reload" -> {
					sender.server.reloadWhitelist()
					broadcastCommandMessage(sender, TranslationContainer("commands.whitelist.reloaded"))
					return true
				}
				"on" -> {
					sender.server.setPropertyBoolean("white-list", true)
					broadcastCommandMessage(sender, TranslationContainer("commands.whitelist.enabled"))
					return true
				}
				"off" -> {
					sender.server.setPropertyBoolean("white-list", false)
					broadcastCommandMessage(sender, TranslationContainer("commands.whitelist.disabled"))
					return true
				}
				"list" -> {
					var result = ""
					var count = 0
					for (player in sender.server.whitelist.all.keys) {
						result += "$player, "
						++count
					}
					sender.sendMessage(TranslationContainer("commands.whitelist.list", count.toString(), count.toString()))
					sender.sendMessage(if (result.length > 0) result.substring(0, result.length - 2) else "")
					return true
				}
				"add" -> {
					sender.sendMessage(TranslationContainer("commands.generic.usage", "%commands.whitelist.add.usage"))
					return true
				}
				"remove" -> {
					sender.sendMessage(TranslationContainer("commands.generic.usage", "%commands.whitelist.remove.usage"))
					return true
				}
			}
		} else if (args.size == 2) {
			if (badPerm(sender, args[0]!!.toLowerCase())) {
				return false
			}
			when (args[0]!!.toLowerCase()) {
				"add" -> {
					sender.server.getOfflinePlayer(args[1]).isWhitelisted = true
					broadcastCommandMessage(sender, TranslationContainer("commands.whitelist.add.success", args[1]))
					return true
				}
				"remove" -> {
					sender.server.getOfflinePlayer(args[1]).isWhitelisted = false
					broadcastCommandMessage(sender, TranslationContainer("commands.whitelist.remove.success", args[1]))
					return true
				}
			}
		}
		return true
	}

	private fun badPerm(sender: CommandSender, perm: String): Boolean {
		if (!sender.hasPermission("nukkit.command.whitelist$perm")) {
			sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.permission"))
			return true
		}
		return false
	}

	init {
		permission = "nukkit.command.whitelist.reload;" +
				"nukkit.command.whitelist.enable;" +
				"nukkit.command.whitelist.disable;" +
				"nukkit.command.whitelist.list;" +
				"nukkit.command.whitelist.add;" +
				"nukkit.command.whitelist.remove"
		commandParameters.clear()
		commandParameters["1arg"] = arrayOf<CommandParameter?>(
				CommandParameter("on|off|list|reload", CommandParamType.STRING, false)
		)
		commandParameters["2args"] = arrayOf<CommandParameter?>(
				CommandParameter("add|remove", CommandParamType.STRING, false),
				CommandParameter("player", CommandParamType.TARGET, false)
		)
	}
}