package cn.nukkit.event.entity

import cn.nukkit.entity.Entity
import cn.nukkit.event.Event

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class EntityEvent : Event() {
	open var entity: Entity? = null
		protected set
}