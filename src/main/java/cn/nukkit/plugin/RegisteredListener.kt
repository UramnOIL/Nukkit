package cn.nukkit.plugin

import cn.nukkit.event.Cancellable
import cn.nukkit.event.Event
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.utils.EventException
import co.aikar.timings.Timing

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class RegisteredListener(val listener: Listener, private val executor: EventExecutor, private val priority: EventPriority, val plugin: Plugin, private val ignoreCancelled: Boolean, private val timing: Timing) {
	@Throws(EventException::class)
	fun callEvent(event: Event) {
		if (event is Cancellable) {
			if (event.isCancelled && ignoreCancelled) {
				return
			}
		}
		timing.startTiming()
		executor.execute(listener, event)
		timing.stopTiming()
	}
}