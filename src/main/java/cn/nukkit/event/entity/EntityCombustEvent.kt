package cn.nukkit.event.entity

import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class EntityCombustEvent(combustee: Entity?, duration: Int) : EntityEvent(), Cancellable {
	var duration: Int

	companion object {
		val handlers = HandlerList()
	}

	init {
		entity = combustee
		this.duration = duration
	}
}