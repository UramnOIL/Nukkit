package cn.nukkit.command.defaults

import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.command.ConsoleCommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import java.util.*
import kotlin.collections.MutableMap
import kotlin.collections.set
import kotlin.collections.toTypedArray

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class HelpCommand(name: String) : VanillaCommand(name, "%nukkit.command.help.description", "%commands.help.usage", arrayOf<String?>("?")) {
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		var args = args
		if (!testPermission(sender)) {
			return true
		}
		var command: String? = ""
		var pageNumber = 1
		var pageHeight = 5
		if (args.size != 0) {
			try {
				pageNumber = Integer.valueOf(args[args.size - 1])
				if (pageNumber <= 0) {
					pageNumber = 1
				}
				val newargs = arrayOfNulls<String>(args.size - 1)
				System.arraycopy(args, 0, newargs, 0, newargs.size)
				args = newargs
				/*if (args.length > 1) {
                    args = Arrays.copyOfRange(args, 0, args.length - 2);
                } else {
                    args = new String[0];
                }*/for (arg in args) {
					if (command != "") {
						command += " "
					}
					command += arg
				}
			} catch (e: NumberFormatException) {
				pageNumber = 1
				for (arg in args) {
					if (command != "") {
						command += " "
					}
					command += arg
				}
			}
		}
		if (sender is ConsoleCommandSender) {
			pageHeight = Int.MAX_VALUE
		}
		return if (command == "") {
			val commands: MutableMap<String?, Command> = TreeMap()
			for (cmd in sender.server.commandMap.commands.values) {
				if (cmd.testPermissionSilent(sender)) {
					commands[cmd.name] = cmd
				}
			}
			val totalPage = if (commands.size % pageHeight == 0) commands.size / pageHeight else commands.size / pageHeight + 1
			pageNumber = Math.min(pageNumber, totalPage)
			if (pageNumber < 1) {
				pageNumber = 1
			}
			sender.sendMessage(TranslationContainer("commands.help.header", pageNumber.toString(), totalPage.toString()))
			var i = 1
			for (command1 in commands.values) {
				if (i >= (pageNumber - 1) * pageHeight + 1 && i <= Math.min(commands.size, pageNumber * pageHeight)) {
					sender.sendMessage(TextFormat.DARK_GREEN.toString() + "/" + command1.name + ": " + TextFormat.WHITE + command1.description)
				}
				i++
			}
			true
		} else {
			val cmd = sender.server.commandMap.getCommand(command!!.toLowerCase())
			if (cmd != null) {
				if (cmd.testPermissionSilent(sender)) {
					var message = """${TextFormat.YELLOW}--------- ${TextFormat.WHITE} Help: /${cmd.name}${TextFormat.YELLOW} ---------
"""
					message += """
						${TextFormat.GOLD}Description: ${TextFormat.WHITE}${cmd.description}

						""".trimIndent()
					var usage = ""
					val usages = cmd.usage.split("\n").toTypedArray()
					for (u in usages) {
						if (usage != "") {
							usage += """

								${TextFormat.WHITE}
								""".trimIndent()
						}
						usage += u
					}
					message += """
						${TextFormat.GOLD}Usage: ${TextFormat.WHITE}$usage

						""".trimIndent()
					sender.sendMessage(message)
					return true
				}
			}
			sender.sendMessage(TextFormat.RED.toString() + "No help for " + command.toLowerCase())
			true
		}
	}

	init {
		permission = "nukkit.command.help"
		commandParameters.clear()
		commandParameters["default"] = arrayOf<CommandParameter?>(
				CommandParameter("page", CommandParamType.INT, true)
		)
	}
}