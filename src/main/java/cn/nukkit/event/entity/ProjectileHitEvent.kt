package cn.nukkit.event.entity

import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.level.MovingObjectPosition

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ProjectileHitEvent @JvmOverloads constructor(entity: EntityProjectile?, movingObjectPosition: MovingObjectPosition? = null) : EntityEvent(), Cancellable {
	var movingObjectPosition: MovingObjectPosition?

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = entity
		this.movingObjectPosition = movingObjectPosition
	}
}