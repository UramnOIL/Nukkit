package cn.nukkit.scheduler

import cn.nukkit.Server
import cn.nukkit.utils.ThreadStore
import co.aikar.timings.Timings
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * @author Nukkit Project Team
 */
abstract class AsyncTask : Runnable {
	var result: Any? = null
	var taskId = 0
	var isFinished = false
		private set

	override fun run() {
		result = null
		onRun()
		isFinished = true
		FINISHED_LIST.offer(this)
	}

	fun hasResult(): Boolean {
		return result != null
	}

	fun getFromThreadStore(identifier: String?): Any? {
		return if (isFinished) null else ThreadStore.store[identifier]
	}

	fun saveToThreadStore(identifier: String?, value: Any?) {
		if (!isFinished) {
			if (value == null) {
				ThreadStore.store.remove(identifier)
			} else {
				ThreadStore.store[identifier] = value
			}
		}
	}

	abstract fun onRun()
	open fun onCompletion(server: Server?) {}
	fun cleanObject() {
		result = null
		taskId = 0
		isFinished = false
	}

	companion object {
		val FINISHED_LIST: Queue<AsyncTask> = ConcurrentLinkedQueue()
		fun collectTask() {
			Timings.schedulerAsyncTimer.startTiming()
			while (!FINISHED_LIST.isEmpty()) {
				val task = FINISHED_LIST.poll()
				try {
					task.onCompletion(Server.instance)
				} catch (e: Exception) {
					Server.instance.logger.critical("Exception while async task "
							+ task.taskId
							+ " invoking onCompletion", e)
				}
			}
			Timings.schedulerAsyncTimer.stopTiming()
		}
	}
}