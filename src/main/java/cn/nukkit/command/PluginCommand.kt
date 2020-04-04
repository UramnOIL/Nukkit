package cn.nukkit.command

import cn.nukkit.lang.TranslationContainer
import cn.nukkit.plugin.Plugin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PluginCommand<T : Plugin?>(name: String, private val owningPlugin: T) : Command(name), PluginIdentifiableCommand {
	private var executor: CommandExecutor
	override fun execute(sender: CommandSender, commandLabel: String?, args: Array<String?>): Boolean {
		if (!owningPlugin!!.isEnabled) {
			return false
		}
		if (!testPermission(sender)) {
			return false
		}
		val success = executor.onCommand(sender, this, commandLabel, args)
		if (!success && usageMessage != "") {
			sender.sendMessage(TranslationContainer("commands.generic.usage", usageMessage))
		}
		return success
	}

	fun getExecutor(): CommandExecutor {
		return executor
	}

	fun setExecutor(executor: CommandExecutor?) {
		this.executor = executor ?: owningPlugin
	}

	override val plugin: Plugin?
		get() = owningPlugin

	init {
		executor = owningPlugin
		usageMessage = ""
	}
}