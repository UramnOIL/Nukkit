package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.permission.BanEntry
import cn.nukkit.permission.BanList
import kotlin.collections.Iterator
import kotlin.collections.set

/**
 * Created on 2015/11/11 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class BanListCommand(name: String) : VanillaCommand(name, "%nukkit.command.banlist.description", "%commands.banlist.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		val list: BanList
		var ips = false
		if (args.size > 0) {
			when (args[0]!!.toLowerCase()) {
				"ips" -> {
					list = sender.server.iPBans
					ips = true
				}
				"players" -> list = sender.server.nameBans
				else -> {
					sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
					return false
				}
			}
		} else {
			list = sender.server.nameBans
		}
		val builder = StringBuilder()
		val itr: Iterator<BanEntry> = list.entires.values.iterator()
		while (itr.hasNext()) {
			builder.append(itr.next().name)
			if (itr.hasNext()) {
				builder.append(", ")
			}
		}
		if (ips) {
			sender.sendMessage(TranslationContainer("commands.banlist.ips", list.entires.size.toString()))
		} else {
			sender.sendMessage(TranslationContainer("commands.banlist.players", list.entires.size.toString()))
		}
		sender.sendMessage(builder.toString())
		return true
	}

	init {
		permission = "nukkit.command.ban.list"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("ips|players", true)
		)
	}
}