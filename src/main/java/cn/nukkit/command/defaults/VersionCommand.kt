package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.network.protocol.ProtocolInfo
import cn.nukkit.plugin.Plugin
import cn.nukkit.utils.TextFormat
import java.util.function.Consumer

/**
 * Created on 2015/11/12 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class VersionCommand(name: String) : VanillaCommand(name,
		"%nukkit.command.version.description",
		"%nukkit.command.version.usage", arrayOf<String?>("ver", "about")) {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size == 0) {
			sender.sendMessage(TranslationContainer("nukkit.server.info.extended", sender.server.name,
					sender.server.nukkitVersion,
					sender.server.codename,
					sender.server.apiVersion,
					sender.server.version, ProtocolInfo.CURRENT_PROTOCOL.toString()))
		} else {
			var pluginName = ""
			for (arg in args) pluginName += "$arg "
			pluginName = pluginName.trim { it <= ' ' }
			val found = booleanArrayOf(false)
			val exactPlugin = arrayOf(sender.server.pluginManager.getPlugin(pluginName))
			if (exactPlugin[0] == null) {
				pluginName = pluginName.toLowerCase()
				val finalPluginName = pluginName
				sender.server.pluginManager.plugins.forEach { (s: String, p: Plugin?) ->
					if (s.toLowerCase().contains(finalPluginName)) {
						exactPlugin[0] = p
						found[0] = true
					}
				}
			} else {
				found[0] = true
			}
			if (found[0]) {
				val desc = exactPlugin[0]!!.description
				sender.sendMessage(TextFormat.DARK_GREEN.toString() + desc.name + TextFormat.WHITE + " version " + TextFormat.DARK_GREEN + desc.version)
				if (desc.description != null) {
					sender.sendMessage(desc.description)
				}
				if (desc.website != null) {
					sender.sendMessage("Website: " + desc.website)
				}
				val authors = desc.authors
				val authorsString = arrayOf("")
				authors.forEach(Consumer { s: String -> authorsString[0] += s })
				if (authors.size == 1) {
					sender.sendMessage("Author: " + authorsString[0])
				} else if (authors.size >= 2) {
					sender.sendMessage("Authors: " + authorsString[0])
				}
			} else {
				sender.sendMessage(TranslationContainer("nukkit.command.version.noSuchPlugin"))
			}
		}
		return true
	}

	init {
		permission = "nukkit.command.version"
		commandParameters.clear()
	}
}