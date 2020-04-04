package cn.nukkit.event.entity

import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.math.Vector3

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EntityMotionEvent(entity: Entity?, motion: Vector3) : EntityEvent(), Cancellable {
	@get:Deprecated("")
	val vector: Vector3
		@Deprecated("") get() = field

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = entity
		vector = motion
	}
}