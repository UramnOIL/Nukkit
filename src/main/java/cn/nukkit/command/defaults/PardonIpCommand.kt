package cn.nukkit.command.defaults

import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import java.util.regex.Pattern
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PardonIpCommand(name: String) : VanillaCommand(name, "%nukkit.command.unban.ip.description", "%commands.unbanip.usage") {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!testPermission(sender)) {
			return true
		}
		if (args.size != 1) {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
			return false
		}
		val value = args[0]
		if (Pattern.matches("^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$", value)) {
			sender.server.iPBans.remove(value)
			sender.server.network.unblockAddress(value)
			broadcastCommandMessage(sender, TranslationContainer("commands.unbanip.success", value))
		} else {
			sender.sendMessage(TranslationContainer("commands.unbanip.invalid"))
		}
		return true
	}

	init {
		permission = "nukkit.command.unban.ip"
		aliases = arrayOf<String?>("unbanip", "unban-ip", "pardonip")
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("ip")
		)
	}
}