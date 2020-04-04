package cn.nukkit.scheduler

import cn.nukkit.Server
import cn.nukkit.plugin.Plugin
import co.aikar.timings.Timing
import co.aikar.timings.Timings

/**
 * @author MagicDroidX
 */
class TaskHandler(val plugin: Plugin?, val task: Runnable, val taskId: Int, val isAsynchronous: Boolean) {
	var delay = 0
	var period = 0
	var lastRunTick = 0
	var nextRunTick = 0
	var isCancelled = false
		private set
	val timing: Timing

	val isDelayed: Boolean
		get() = delay > 0

	val isRepeating: Boolean
		get() = period > 0

	fun cancel() {
		if (!isCancelled && task is Task) {
			task.onCancel()
		}
		isCancelled = true
	}

	@Deprecated("")
	fun remove() {
		isCancelled = true
	}

	fun run(currentTick: Int) {
		try {
			lastRunTick = currentTick
			task.run()
		} catch (ex: RuntimeException) {
			Server.instance.logger.critical("Exception while invoking run", ex)
		}
	}

	@get:Deprecated("")
	val taskName: String
		get() = "Unknown"

	init {
		timing = Timings.getTaskTiming(this, period.toLong())
	}
}