package cn.nukkit.plugin

import cn.nukkit.Server
import cn.nukkit.utils.LogLevel
import cn.nukkit.utils.Logger

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PluginLogger(context: Plugin) : Logger {
	private val pluginName: String
	override fun emergency(message: String) {
		this.log(LogLevel.EMERGENCY, message)
	}

	override fun alert(message: String) {
		this.log(LogLevel.ALERT, message)
	}

	override fun critical(message: String) {
		this.log(LogLevel.CRITICAL, message)
	}

	override fun error(message: String) {
		this.log(LogLevel.ERROR, message)
	}

	override fun warning(message: String) {
		this.log(LogLevel.WARNING, message)
	}

	override fun notice(message: String) {
		this.log(LogLevel.NOTICE, message)
	}

	override fun info(message: String) {
		this.log(LogLevel.INFO, message)
	}

	override fun debug(message: String) {
		this.log(LogLevel.DEBUG, message)
	}

	override fun log(level: LogLevel, message: String) {
		Server.instance!!.logger.log(level, pluginName + message)
	}

	override fun emergency(message: String, t: Throwable) {
		this.log(LogLevel.EMERGENCY, message, t)
	}

	override fun alert(message: String, t: Throwable) {
		this.log(LogLevel.ALERT, message, t)
	}

	override fun critical(message: String, t: Throwable) {
		this.log(LogLevel.CRITICAL, message, t)
	}

	override fun error(message: String, t: Throwable) {
		this.log(LogLevel.ERROR, message, t)
	}

	override fun warning(message: String, t: Throwable) {
		this.log(LogLevel.WARNING, message, t)
	}

	override fun notice(message: String, t: Throwable) {
		this.log(LogLevel.NOTICE, message, t)
	}

	override fun info(message: String, t: Throwable) {
		this.log(LogLevel.INFO, message, t)
	}

	override fun debug(message: String, t: Throwable) {
		this.log(LogLevel.DEBUG, message, t)
	}

	override fun log(level: LogLevel, message: String, t: Throwable) {
		Server.instance!!.logger.log(level, pluginName + message, t)
	}

	init {
		val prefix = context.description!!.prefix
		pluginName = if (prefix != null) "[$prefix] " else "[" + context.description!!.name + "] "
	}
}