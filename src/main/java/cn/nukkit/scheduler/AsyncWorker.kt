package cn.nukkit.scheduler

import cn.nukkit.InterruptibleThread
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class AsyncWorker : Thread(), InterruptibleThread {
	private val stack = LinkedList<AsyncTask>()
	fun stack(task: AsyncTask) {
		synchronized(stack) { stack.addFirst(task) }
	}

	fun unstack() {
		synchronized(stack) { stack.clear() }
	}

	fun unstack(task: AsyncTask) {
		synchronized(stack) { stack.remove(task) }
	}

	override fun run() {
		while (true) {
			synchronized(stack) {
				for (task in stack) {
					if (!task.isFinished) {
						task.run()
					}
				}
			}
			try {
				sleep(5)
			} catch (e: InterruptedException) {
				//igonre
			}
		}
	}

	init {
		name = "Asynchronous Worker"
	}
}