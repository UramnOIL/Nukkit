package cn.nukkit.command

import cn.nukkit.Server
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.command.defaults.*
import cn.nukkit.command.simple.*
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import cn.nukkit.utils.Utils
import java.util.*
import java.util.function.Function
import java.util.function.IntFunction
import java.util.stream.Collectors
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.set
import kotlin.collections.toTypedArray

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class SimpleCommandMap(private val server: Server) : CommandMap {
	protected val knownCommands: MutableMap<String?, Command> = HashMap()
	private fun setDefaultCommands() {
		this.register("nukkit", VersionCommand("version"))
		this.register("nukkit", PluginsCommand("plugins"))
		this.register("nukkit", SeedCommand("seed"))
		this.register("nukkit", HelpCommand("help"))
		this.register("nukkit", StopCommand("stop"))
		this.register("nukkit", TellCommand("tell"))
		this.register("nukkit", DefaultGamemodeCommand("defaultgamemode"))
		this.register("nukkit", BanCommand("ban"))
		this.register("nukkit", BanIpCommand("ban-ip"))
		this.register("nukkit", BanListCommand("banlist"))
		this.register("nukkit", PardonCommand("pardon"))
		this.register("nukkit", PardonIpCommand("pardon-ip"))
		this.register("nukkit", SayCommand("say"))
		this.register("nukkit", MeCommand("me"))
		this.register("nukkit", ListCommand("list"))
		this.register("nukkit", DifficultyCommand("difficulty"))
		this.register("nukkit", KickCommand("kick"))
		this.register("nukkit", OpCommand("op"))
		this.register("nukkit", DeopCommand("deop"))
		this.register("nukkit", WhitelistCommand("whitelist"))
		this.register("nukkit", SaveOnCommand("save-on"))
		this.register("nukkit", SaveOffCommand("save-off"))
		this.register("nukkit", SaveCommand("save-all"))
		this.register("nukkit", GiveCommand("give"))
		this.register("nukkit", EffectCommand("effect"))
		this.register("nukkit", EnchantCommand("enchant"))
		this.register("nukkit", ParticleCommand("particle"))
		this.register("nukkit", GamemodeCommand("gamemode"))
		this.register("nukkit", GameruleCommand("gamerule"))
		this.register("nukkit", KillCommand("kill"))
		this.register("nukkit", SpawnpointCommand("spawnpoint"))
		this.register("nukkit", SetWorldSpawnCommand("setworldspawn"))
		this.register("nukkit", TeleportCommand("tp"))
		this.register("nukkit", TimeCommand("time"))
		this.register("nukkit", TitleCommand("title"))
		this.register("nukkit", ReloadCommand("reload"))
		this.register("nukkit", WeatherCommand("weather"))
		this.register("nukkit", XpCommand("xp"))

//        if ((boolean) this.server.getConfig("debug.commands", false)) {
		this.register("nukkit", StatusCommand("status"))
		this.register("nukkit", GarbageCollectorCommand("gc"))
		this.register("nukkit", TimingsCommand("timings"))
		this.register("nukkit", DebugPasteCommand("debugpaste"))
		//this.register("nukkit", new DumpMemoryCommand("dumpmemory"));
//        }
	}

	override fun registerAll(fallbackPrefix: String, commands: List<Command>) {
		for (command in commands) {
			this.register(fallbackPrefix, command)
		}
	}

	override fun register(fallbackPrefix: String, command: Command): Boolean {
		return this.register(fallbackPrefix, command, null)
	}

	override fun register(fallbackPrefix: String, command: Command, label: String?): Boolean {
		var fallbackPrefix = fallbackPrefix
		var label = label
		if (label == null) {
			label = command.name
		}
		label = label!!.trim { it <= ' ' }.toLowerCase()
		fallbackPrefix = fallbackPrefix.trim { it <= ' ' }.toLowerCase()
		val registered = registerAlias(command, false, fallbackPrefix, label)
		val aliases: MutableList<String?> = ArrayList(Arrays.asList(*command.aliases))
		val iterator = aliases.iterator()
		while (iterator.hasNext()) {
			val alias = iterator.next()
			if (!registerAlias(command, true, fallbackPrefix, alias)) {
				iterator.remove()
			}
		}
		command.aliases = aliases.toTypedArray()
		if (!registered) {
			command.label = "$fallbackPrefix:$label"
		}
		command.register(this)
		return registered
	}

	override fun registerSimpleCommands(`object`: Any) {
		for (method in `object`.javaClass.declaredMethods) {
			val def = method.getAnnotation(cn.nukkit.command.simple.Command::class.java)
			if (def != null) {
				val sc = SimpleCommand(`object`, method, def.name(), def.description(), def.usageMessage(), def.aliases())
				val args = method.getAnnotation(Arguments::class.java)
				if (args != null) {
					sc.setMaxArgs(args.max())
					sc.setMinArgs(args.min())
				}
				val perm = method.getAnnotation(CommandPermission::class.java)
				if (perm != null) {
					sc.permission = perm.value()
				}
				if (method.isAnnotationPresent(ForbidConsole::class.java)) {
					sc.setForbidConsole(true)
				}
				val commandParameters = method.getAnnotation(CommandParameters::class.java)
				if (commandParameters != null) {
					val map: Map<String, Array<CommandParameter?>?> = Arrays.stream(commandParameters.parameters())
							.collect(Collectors.toMap(Function<Parameters, String> { obj: Parameters -> obj.name() }, Function<Parameters, Array<CommandParameter>> { parameters: Parameters ->
								Arrays.stream(parameters.parameters())
										.map(Function<Parameter, CommandParameter> { parameter: Parameter -> CommandParameter(parameter.name(), parameter.type(), parameter.optional()) })
										.distinct()
										.toArray(IntFunction<Array<CommandParameter>> { _Dummy_.__Array__() })
							}))
					sc.commandParameters.putAll(map)
				}
				this.register(def.name(), sc)
			}
		}
	}

	private fun registerAlias(command: Command, isAlias: Boolean, fallbackPrefix: String, label: String?): Boolean {
		knownCommands["$fallbackPrefix:$label"] = command

		//if you're registering a command alias that is already registered, then return false
		val alreadyRegistered = knownCommands.containsKey(label)
		val existingCommand = knownCommands[label]
		val existingCommandIsNotVanilla = alreadyRegistered && existingCommand !is VanillaCommand
		//basically, if we're an alias and it's already registered, or we're a vanilla command, then we can't override it
		if ((command is VanillaCommand || isAlias) && alreadyRegistered && existingCommandIsNotVanilla) {
			return false
		}

		//if you're registering a name (alias or label) which is identical to another command who's primary name is the same
		//so basically we can't override the main name of a command, but we can override aliases if we're not an alias

		//added the last statement which will allow us to override a VanillaCommand unconditionally
		if (alreadyRegistered && existingCommand.getLabel() != null && existingCommand.getLabel() == label && existingCommandIsNotVanilla) {
			return false
		}

		//you can now assume that the command is either uniquely named, or overriding another command's alias (and is not itself, an alias)
		if (!isAlias) {
			command.label = label
		}

		// Then we need to check if there isn't any command conflicts with vanilla commands
		val toRemove = ArrayList<String?>()
		for ((key, cmd) in knownCommands) {
			if (cmd.label.equals(command.label, ignoreCase = true) && cmd != command) { // If the new command conflicts... (But if it isn't the same command)
				if (cmd is VanillaCommand) { // And if the old command is a vanilla command...
					// Remove it!
					toRemove.add(key)
				}
			}
		}

		// Now we loop the toRemove list to remove the command conflicts from the knownCommands map
		for (cmd in toRemove) {
			knownCommands.remove(cmd)
		}
		knownCommands[label] = command
		return true
	}

	private fun parseArguments(cmdLine: String): ArrayList<String?> {
		val sb = StringBuilder(cmdLine)
		val args = ArrayList<String?>()
		var notQuoted = true
		var start = 0
		var i = 0
		while (i < sb.length) {
			if (sb[i] == '\\') {
				sb.deleteCharAt(i)
				i++
				continue
			}
			if (sb[i] == ' ' && notQuoted) {
				val arg = sb.substring(start, i)
				if (!arg.isEmpty()) {
					args.add(arg)
				}
				start = i + 1
			} else if (sb[i] == '"') {
				sb.deleteCharAt(i)
				--i
				notQuoted = !notQuoted
			}
			i++
		}
		val arg = sb.substring(start)
		if (!arg.isEmpty()) {
			args.add(arg)
		}
		return args
	}

	override fun dispatch(sender: CommandSender, cmdLine: String): Boolean {
		val parsed = parseArguments(cmdLine)
		if (parsed.size == 0) {
			return false
		}
		val sentCommandLabel = parsed.removeAt(0)!!.toLowerCase()
		val args = parsed.toTypedArray()
		val target = getCommand(sentCommandLabel) ?: return false
		target.timing.startTiming()
		try {
			target.execute(sender, sentCommandLabel, args)
		} catch (e: Exception) {
			sender.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.exception"))
			server.logger.critical(server.language.translateString("nukkit.command.exception", cmdLine, target.toString(), Utils.getExceptionMessage(e)))
			val logger = sender.server.logger
			logger?.logException(e)
		}
		target.timing.stopTiming()
		return true
	}

	override fun clearCommands() {
		for (command in knownCommands.values) {
			command.unregister(this)
		}
		knownCommands.clear()
		setDefaultCommands()
	}

	override fun getCommand(name: String?): Command? {
		return if (knownCommands.containsKey(name)) {
			knownCommands[name]
		} else null
	}

	val commands: Map<String?, Command>
		get() = knownCommands

	fun registerServerAliases() {
		val values = server.commandAliases
		for ((alias, commandStrings) in values) {
			if (alias.contains(" ") || alias.contains(":")) {
				server.logger.warning(server.language.translateString("nukkit.command.alias.illegal", alias))
				continue
			}
			val targets: MutableList<String> = ArrayList()
			var bad = ""
			for (commandString in commandStrings) {
				val args = commandString.split(" ").toTypedArray()
				val command = getCommand(args[0])
				if (command == null) {
					if (bad.length > 0) {
						bad += ", "
					}
					bad += commandString
				} else {
					targets.add(commandString)
				}
			}
			if (bad.length > 0) {
				server.logger.warning(server.language.translateString("nukkit.command.alias.notFound", *arrayOf(alias, bad)))
				continue
			}
			if (!targets.isEmpty()) {
				knownCommands[alias.toLowerCase()] = FormattedCommandAlias(alias.toLowerCase(), targets)
			} else {
				knownCommands.remove(alias.toLowerCase())
			}
		}
	}

	init {
		setDefaultCommands()
	}
}