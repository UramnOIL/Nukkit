package cn.nukkit.command

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.command.data.*
import cn.nukkit.lang.TextContainer
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.utils.TextFormat
import co.aikar.timings.Timing
import co.aikar.timings.Timings
import java.util.*
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.collections.toTypedArray

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class Command @JvmOverloads constructor(name: String, description: String = "", usageMessage: String? = null, aliases: Array<String?> = arrayOfNulls(0)) {
	/**
	 * Returns an CommandData containing command data
	 *
	 * @return CommandData
	 */
	var defaultCommandData: CommandData
		protected set
	val name: String
	private var nextLabel: String?
	var label: String?
		private set
	private var aliases = arrayOfNulls<String>(0)
	private var activeAliases = arrayOfNulls<String>(0)
	private var commandMap: CommandMap? = null
	var description = ""
	var usage = ""
	var permission: String? = null
	var permissionMessage: String? = null
	var commandParameters: MutableMap<String, Array<CommandParameter?>> = HashMap()
	var timing: Timing

	fun getCommandParameters(key: String): Array<CommandParameter?> {
		return commandParameters[key]!!
	}

	fun getCommandParameters(): Map<String, Array<CommandParameter?>> {
		return commandParameters
	}

	fun setCommandParameters(commandParameters: MutableMap<String, Array<CommandParameter?>>) {
		this.commandParameters = commandParameters
	}

	fun addCommandParameters(key: String, parameters: Array<CommandParameter?>) {
		commandParameters[key] = parameters
	}

	/**
	 * Generates modified command data for the specified player
	 * for AvailableCommandsPacket.
	 *
	 * @param player player
	 * @return CommandData|null
	 */
	fun generateCustomCommandData(player: Player): CommandDataVersions? {
		if (!testPermission(player)) {
			return null
		}
		val customData = defaultCommandData.clone()
		if (getAliases().size > 0) {
			val aliases: MutableList<String> = ArrayList(Arrays.asList(*getAliases()))
			if (!aliases.contains(name)) {
				aliases.add(name)
			}
			customData.aliases = CommandEnum(name + "Aliases", aliases)
		}
		customData.description = player.getServer().language.translateString(description)
		commandParameters.forEach { (key: String?, par: Array<CommandParameter?>?) ->
			val overload = CommandOverload()
			overload.input.parameters = par
			customData.overloads[key] = overload
		}
		if (customData.overloads.size == 0) customData.overloads["default"] = CommandOverload()
		val versions = CommandDataVersions()
		versions.versions.add(customData)
		return versions
	}

	val overloads: Map<String?, CommandOverload?>?
		get() = defaultCommandData.overloads

	abstract fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean

	fun testPermission(target: CommandSender): Boolean {
		if (testPermissionSilent(target)) {
			return true
		}
		if (permissionMessage == null) {
			target.sendMessage(TranslationContainer(TextFormat.RED.toString() + "%commands.generic.unknown", name))
		} else if (permissionMessage != "") {
			target.sendMessage(permissionMessage!!.replace("<permission>", permission!!))
		}
		return false
	}

	fun testPermissionSilent(target: CommandSender): Boolean {
		if (permission == null || permission == "") {
			return true
		}
		val permissions = permission!!.split(";").toTypedArray()
		for (permission in permissions) {
			if (target.hasPermission(permission)) {
				return true
			}
		}
		return false
	}

	fun setLabel(name: String?): Boolean {
		nextLabel = name
		if (!isRegistered) {
			label = name
			timing = Timings.getCommandTiming(this)
			return true
		}
		return false
	}

	fun register(commandMap: CommandMap?): Boolean {
		if (allowChangesFrom(commandMap)) {
			this.commandMap = commandMap
			return true
		}
		return false
	}

	fun unregister(commandMap: CommandMap?): Boolean {
		if (allowChangesFrom(commandMap)) {
			this.commandMap = null
			activeAliases = aliases
			label = nextLabel
			return true
		}
		return false
	}

	fun allowChangesFrom(commandMap: CommandMap?): Boolean {
		return commandMap != null && commandMap != this.commandMap
	}

	val isRegistered: Boolean
		get() = commandMap != null

	fun getAliases(): Array<String?> {
		return activeAliases
	}

	fun setAliases(aliases: Array<String?>) {
		this.aliases = aliases
		if (!isRegistered) {
			activeAliases = aliases
		}
	}

	override fun toString(): String {
		return name
	}

	companion object {
		private val defaultDataTemplate: CommandData? = null
		fun generateDefaultData(): CommandData? {
			if (defaultDataTemplate == null) {
				//defaultDataTemplate = new Gson().fromJson(new InputStreamReader(Server.class.getClassLoader().getResourceAsStream("command_default.json")));
			}
			return defaultDataTemplate!!.clone()
		}

		@JvmOverloads
		fun broadcastCommandMessage(source: CommandSender, message: String, sendToSource: Boolean = true) {
			val users = source.server.pluginManager.getPermissionSubscriptions(Server.BROADCAST_CHANNEL_ADMINISTRATIVE)
			val result = TranslationContainer("chat.type.admin", source.name, message)
			val colored = TranslationContainer(TextFormat.GRAY.toString() + "" + TextFormat.ITALIC + "%chat.type.admin", source.name, message)
			if (sendToSource && source !is ConsoleCommandSender) {
				source.sendMessage(message)
			}
			for (user in users) {
				if (user is CommandSender) {
					if (user is ConsoleCommandSender) {
						user.sendMessage(result)
					} else if (user != source) {
						user.sendMessage(colored)
					}
				}
			}
		}

		@JvmOverloads
		fun broadcastCommandMessage(source: CommandSender, message: TextContainer, sendToSource: Boolean = true) {
			val m = message.clone()
			val resultStr = "[" + source.name + ": " + (if (m.text != source.server.language[m.text]) "%" else "") + m.text + "]"
			val users = source.server.pluginManager.getPermissionSubscriptions(Server.BROADCAST_CHANNEL_ADMINISTRATIVE)
			val coloredStr = TextFormat.GRAY.toString() + "" + TextFormat.ITALIC + resultStr
			m.text = resultStr
			val result = m.clone()
			m.text = coloredStr
			val colored = m.clone()
			if (sendToSource && source !is ConsoleCommandSender) {
				source.sendMessage(message)
			}
			for (user in users) {
				if (user is CommandSender) {
					if (user is ConsoleCommandSender) {
						user.sendMessage(result)
					} else if (user != source) {
						user.sendMessage(colored)
					}
				}
			}
		}
	}

	init {
		defaultCommandData = CommandData()
		this.name = name.toLowerCase() // Uppercase letters crash the client?!?
		nextLabel = name
		label = name
		this.description = description
		usage = usageMessage ?: "/$name"
		this.aliases = aliases
		activeAliases = aliases
		timing = Timings.getCommandTiming(this)
		commandParameters["default"] = arrayOf(CommandParameter("args", CommandParamType.RAWTEXT, true))
	}
}