package cn.nukkit.command

/**
 * author: MagicDroidX
 * Nukkit Project
 */
interface CommandMap {
	fun registerAll(fallbackPrefix: String, commands: List<Command>)
	fun register(fallbackPrefix: String, command: Command): Boolean
	fun register(fallbackPrefix: String, command: Command, label: String?): Boolean
	fun registerSimpleCommands(`object`: Any)
	fun dispatch(sender: CommandSender, cmdLine: String): Boolean
	fun clearCommands()
	fun getCommand(name: String?): Command?
}