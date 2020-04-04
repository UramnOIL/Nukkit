package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat

/**
 * Created on 2015/11/12 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
class PluginsCommand(name: String) : VanillaCommand(name,
		"%nukkit.command.plugins.description",
		"%nukkit.command.plugins.usage", arrayOf<String?>("pl")) {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		sendPluginList(sender)
		return true
	}

	private fun sendPluginList(sender: CommandSender) {
		var list = ""
		val plugins = sender.server.pluginManager.plugins
		for (plugin in plugins.values) {
			if (list.length > 0) {
				list += TextFormat.WHITE.toString() + ", "
			}
			list += if (plugin.isEnabled) TextFormat.GREEN else TextFormat.RED
			list += plugin.description.fullName
		}
		sender.sendMessage(TranslationContainer("nukkit.command.plugins.success", plugins.size.toString(), list))
	}

	init {
		permission = "nukkit.command.plugins"
		commandParameters.clear()
	}
}