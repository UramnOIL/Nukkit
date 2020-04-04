package cn.nukkit.command

import cn.nukkit.lang.TextContainer

/**
 * Represents an RCON command sender.
 *
 * @author Tee7even
 */
class RemoteConsoleCommandSender : ConsoleCommandSender() {
	private val messages = StringBuilder()
	override fun sendMessage(message: String) {
		var message = message
		message = this.server.language.translateString(message)
		messages.append(message.trim { it <= ' ' }).append("\n")
	}

	override fun sendMessage(message: TextContainer?) {
		this.sendMessage(this.server.language.translate(message))
	}

	fun getMessages(): String {
		return messages.toString()
	}

	override val name: String
		get() = "Rcon"
}