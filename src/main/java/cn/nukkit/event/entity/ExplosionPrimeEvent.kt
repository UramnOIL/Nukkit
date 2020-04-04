package cn.nukkit.event.entity

import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * author: Box
 * Nukkit Project
 *
 *
 * Called when a entity decides to explode
 */
class ExplosionPrimeEvent(entity: Entity?, force: Double) : EntityEvent(), Cancellable {
	var force: Double
	var isBlockBreaking: Boolean

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = entity
		this.force = force
		isBlockBreaking = true
	}
}