package cn.nukkit.utils

import lombok.AccessLevel
import lombok.NoArgsConstructor
import lombok.extern.log4j.Log4j2

/**
 * author: MagicDroidX
 * Nukkit
 */
/*
We need to keep this class for backwards compatibility
 */
@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MainLogger : ThreadedLogger() {
	override fun emergency(message: String?) {
		log.fatal(message)
	}

	override fun alert(message: String?) {
		log.error(message)
	}

	override fun critical(message: String?) {
		log.fatal(message)
	}

	override fun error(message: String?) {
		log.error(message)
	}

	override fun warning(message: String?) {
		log.warn(message)
	}

	override fun notice(message: String?) {
		log.warn(message)
	}

	override fun info(message: String?) {
		log.info(message)
	}

	override fun debug(message: String?) {
		log.debug(message)
	}

	fun setLogDebug(logDebug: Boolean?) {
		throw UnsupportedOperationException()
	}

	fun logException(t: Throwable?) {
		log.throwing(t)
	}

	override fun log(level: LogLevel, message: String) {
		level.log(this, message)
	}

	fun shutdown() {
		throw UnsupportedOperationException()
	}

	override fun emergency(message: String?, t: Throwable?) {
		log.fatal(message, t)
	}

	override fun alert(message: String?, t: Throwable?) {
		log.error(message, t)
	}

	override fun critical(message: String?, t: Throwable?) {
		log.fatal(message, t)
	}

	override fun error(message: String?, t: Throwable?) {
		log.error(message, t)
	}

	override fun warning(message: String?, t: Throwable?) {
		log.warn(message, t)
	}

	override fun notice(message: String?, t: Throwable?) {
		log.warn(message, t)
	}

	override fun info(message: String?, t: Throwable?) {
		log.info(message, t)
	}

	override fun debug(message: String?, t: Throwable?) {
		log.debug(message, t)
	}

	override fun log(level: LogLevel, message: String, t: Throwable) {
		level.log(this, message, t)
	}

	companion object {
		private val logger = MainLogger()
		@JvmStatic
		fun getLogger(): MainLogger {
			return logger
		}
	}
}