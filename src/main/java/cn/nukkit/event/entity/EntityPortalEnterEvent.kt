package cn.nukkit.event.entity

import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class EntityPortalEnterEvent(entity: Entity?, type: PortalType) : EntityEvent(), Cancellable {
	val portalType: PortalType

	enum class PortalType {
		NETHER, END
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = entity
		portalType = type
	}
}