package cn.nukkit.command

import cn.nukkit.Server
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class FormattedCommandAlias : Command {
	private val formatStrings: Array<String>

	constructor(alias: String, formatStrings: Array<String>) : super(alias) {
		this.formatStrings = formatStrings
	}

	constructor(alias: String, formatStrings: List<String>) : super(alias) {
		this.formatStrings = formatStrings.toTypedArray()
	}

	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		var result = false
		val commands = ArrayList<String>()
		for (formatString in formatStrings) {
			try {
				commands.add(buildCommand(formatString, args))
			} catch (e: Exception) {
				if (e is IllegalArgumentException) {
					sender.sendMessage(TextFormat.RED.toString() + e.message)
				} else {
					sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.exception"))
					val logger = sender.server.logger
					logger?.logException(e)
				}
				return false
			}
		}
		for (command in commands) {
			result = result or Server.instance!!.dispatchCommand(sender, command)
		}
		return result
	}

	private fun buildCommand(formatString: String, args: Array<String?>): String {
		var formatString = formatString
		var index = formatString.indexOf("$")
		while (index != -1) {
			val start = index
			if (index > 0 && formatString[start - 1] == '\\') {
				formatString = formatString.substring(0, start - 1) + formatString.substring(start)
				index = formatString.indexOf("$", index)
				continue
			}
			var required = false
			if (formatString[index + 1] == '$') {
				required = true
				// Move index past the second $
				index++
			}

			// Move index past the $
			index++
			val argStart = index
			while (index < formatString.length && inRange(formatString[index].toInt() - 48, 0, 9)) {
				// Move index past current digit
				index++
			}

			// No numbers found
			require(argStart != index) { "Invalid replacement token" }
			var position = Integer.valueOf(formatString.substring(argStart, index))

			// Arguments are not 0 indexed
			require(position != 0) { "Invalid replacement token" }

			// Convert position to 0 index
			position--
			var rest = false
			if (index < formatString.length && formatString[index] == '-') {
				rest = true
				// Move index past the -
				index++
			}
			val end = index
			require(!(required && position >= args.size)) { "Missing required argument " + (position + 1) }
			val replacement = StringBuilder()
			if (rest && position < args.size) {
				for (i in position until args.size) {
					if (i != position) {
						replacement.append(' ')
					}
					replacement.append(args[i])
				}
			} else if (position < args.size) {
				replacement.append(args[position])
			}
			formatString = formatString.substring(0, start) + replacement.toString() + formatString.substring(end)
			// Move index past the replaced data so we don't process it again
			index = start + replacement.length

			// Move to the next replacement token
			index = formatString.indexOf("$", index)
		}
		return formatString
	}

	companion object {
		private fun inRange(i: Int, j: Int, k: Int): Boolean {
			return i >= j && i <= k
		}
	}
}