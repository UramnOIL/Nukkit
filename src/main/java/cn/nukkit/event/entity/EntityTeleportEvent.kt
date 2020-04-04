package cn.nukkit.event.entity

import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.level.Location

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EntityTeleportEvent(entity: Entity?, from: Location, to: Location) : EntityEvent(), Cancellable {
	var from: Location
	var to: Location

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = entity
		this.from = from
		this.to = to
	}
}