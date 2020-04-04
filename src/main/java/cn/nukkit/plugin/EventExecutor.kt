package cn.nukkit.plugin

import cn.nukkit.event.Event
import cn.nukkit.event.Listener
import cn.nukkit.utils.EventException

/**
 * author: iNevet
 * Nukkit Project
 */
interface EventExecutor {
	@Throws(EventException::class)
	fun execute(listener: Listener?, event: Event)
}