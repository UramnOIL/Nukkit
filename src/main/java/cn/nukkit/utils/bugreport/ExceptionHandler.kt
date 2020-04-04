package cn.nukkit.utils.bugreport

/**
 * Project nukkit
 */
class ExceptionHandler : Thread.UncaughtExceptionHandler {
	override fun uncaughtException(thread: Thread, throwable: Throwable) {
		handle(thread, throwable)
	}

	fun handle(thread: Thread?, throwable: Throwable) {
		throwable.printStackTrace()
		try {
			BugReportGenerator(throwable).start()
		} catch (exception: Exception) {
			// Fail Safe
		}
	}

	companion object {
		fun registerExceptionHandler() {
			Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler())
		}
	}
}