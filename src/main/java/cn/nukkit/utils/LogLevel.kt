package cn.nukkit.utils

import org.apache.logging.log4j.util.TriConsumer
import java.util.function.BiConsumer

/**
 * author: MagicDroidX
 * Nukkit Project
 */
enum class LogLevel(private val logTo: BiConsumer<MainLogger, String>, private val logThrowableTo: TriConsumer<MainLogger, String, Throwable>) : Comparable<LogLevel?> {
	NONE(BiConsumer<MainLogger, String> { logger: MainLogger?, message: String? -> }, TriConsumer<MainLogger, String, Throwable> { mainLogger: MainLogger?, s: String?, throwable: Throwable? -> }), EMERGENCY(BiConsumer<MainLogger, String> { obj: MainLogger, message: String? -> obj.emergency(message) }, TriConsumer<MainLogger, String, Throwable> { obj: MainLogger, message: String?, t: Throwable? -> obj.emergency(message, t) }), ALERT(BiConsumer<MainLogger, String> { obj: MainLogger, message: String? -> obj.alert(message) }, TriConsumer<MainLogger, String, Throwable> { obj: MainLogger, message: String?, t: Throwable? -> obj.alert(message, t) }), CRITICAL(BiConsumer<MainLogger, String> { obj: MainLogger, message: String? -> obj.critical(message) }, TriConsumer<MainLogger, String, Throwable> { obj: MainLogger, message: String?, t: Throwable? -> obj.critical(message, t) }), ERROR(BiConsumer<MainLogger, String> { obj: MainLogger, message: String? -> obj.error(message) }, TriConsumer<MainLogger, String, Throwable> { obj: MainLogger, message: String?, t: Throwable? -> obj.error(message, t) }), WARNING(BiConsumer<MainLogger, String> { obj: MainLogger, message: String? -> obj.warning(message) }, TriConsumer<MainLogger, String, Throwable> { obj: MainLogger, message: String?, t: Throwable? -> obj.warning(message, t) }), NOTICE(BiConsumer<MainLogger, String> { obj: MainLogger, message: String? -> obj.notice(message) }, TriConsumer<MainLogger, String, Throwable> { obj: MainLogger, message: String?, t: Throwable? -> obj.notice(message, t) }), INFO(BiConsumer<MainLogger, String> { obj: MainLogger, message: String? -> obj.info(message) }, TriConsumer<MainLogger, String, Throwable> { obj: MainLogger, message: String?, t: Throwable? -> obj.info(message, t) }), DEBUG(BiConsumer<MainLogger, String> { obj: MainLogger, message: String? -> obj.debug(message) }, TriConsumer<MainLogger, String, Throwable> { obj: MainLogger, message: String?, t: Throwable? -> obj.debug(message, t) });

	fun log(logger: MainLogger, message: String) {
		logTo.accept(logger, message)
	}

	fun log(logger: MainLogger, message: String, throwable: Throwable) {
		logThrowableTo.accept(logger, message, throwable)
	}

	fun getLevel(): Int {
		return ordinal
	}

	companion object {
		val DEFAULT_LEVEL = INFO
	}

}