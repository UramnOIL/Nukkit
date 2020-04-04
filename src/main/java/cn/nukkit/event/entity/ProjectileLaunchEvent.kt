package cn.nukkit.event.entity

import cn.nukkit.entity.Entity
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class ProjectileLaunchEvent(entity: EntityProjectile?) : EntityEvent(), Cancellable {
	override var entity: Entity?
		get() = field as EntityProjectile?
		set(entity) {
			super.entity = entity
		}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = entity
	}
}