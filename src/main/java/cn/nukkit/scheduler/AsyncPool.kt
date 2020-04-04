package cn.nukkit.scheduler

import cn.nukkit.Server
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author Nukkit Project Team
 */
class AsyncPool(server: Server, size: Int) : ThreadPoolExecutor(size, Int.MAX_VALUE, 60, TimeUnit.MILLISECONDS, SynchronousQueue()) {
	val server: Server
	override fun afterExecute(runnable: Runnable, throwable: Throwable) {
		if (throwable != null) {
			server.logger.critical("Exception in asynchronous task", throwable)
		}
	}

	init {
		threadFactory = ThreadFactory { runnable: Runnable? ->
			object : Thread(runnable) {
				init {
					isDaemon = true
					name = String.format("Nukkit Asynchronous Task Handler #%s", poolSize)
				}
			}
		}
		this.server = server
	}
}