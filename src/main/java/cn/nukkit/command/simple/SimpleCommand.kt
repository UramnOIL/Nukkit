package cn.nukkit.command.simple

import cn.nukkit.Server
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.command.ConsoleCommandSender
import cn.nukkit.lang.TranslationContainer
import java.lang.reflect.Method

/**
 * @author Tee7even
 */
class SimpleCommand(private val `object`: Any, private val method: Method, name: String, description: String, usageMessage: String?, aliases: Array<String?>) : Command(name, description, usageMessage, aliases) {
	private var forbidConsole = false
	private var maxArgs = 0
	private var minArgs = 0
	fun setForbidConsole(forbidConsole: Boolean) {
		this.forbidConsole = forbidConsole
	}

	fun setMaxArgs(maxArgs: Int) {
		this.maxArgs = maxArgs
	}

	fun setMinArgs(minArgs: Int) {
		this.minArgs = minArgs
	}

	fun sendUsageMessage(sender: CommandSender) {
		if (usageMessage != "") {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
		}
	}

	fun sendInGameMessage(sender: CommandSender) {
		sender.sendMessage(TranslationContainer("commands.generic.ingame"))
	}

	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (forbidConsole && sender is ConsoleCommandSender) {
			sendInGameMessage(sender)
			return false
		} else if (!testPermission(sender)) {
			return false
		} else if (maxArgs != 0 && args.size > maxArgs) {
			sendUsageMessage(sender)
			return false
		} else if (minArgs != 0 && args.size < minArgs) {
			sendUsageMessage(sender)
			return false
		}
		var success = false
		try {
			success = method.invoke(`object`, sender, commandLabel, args) as Boolean
		} catch (exception: Exception) {
			Server.instance!!.logger.logException(exception)
		}
		if (!success) {
			sendUsageMessage(sender)
		}
		return success
	}

}