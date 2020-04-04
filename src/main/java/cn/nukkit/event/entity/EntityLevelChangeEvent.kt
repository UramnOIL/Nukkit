package cn.nukkit.event.entity

import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.level.Level

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EntityLevelChangeEvent(entity: Entity?, originLevel: Level, targetLevel: Level) : EntityEvent(), Cancellable {
	val origin: Level
	val target: Level

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = entity
		origin = originLevel
		target = targetLevel
	}
}