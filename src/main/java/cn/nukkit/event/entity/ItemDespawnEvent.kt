package cn.nukkit.event.entity

import cn.nukkit.entity.Entity
import cn.nukkit.entity.item.EntityItem
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemDespawnEvent(item: EntityItem?) : EntityEvent(), Cancellable {
	override var entity: Entity?
		get() = field as EntityItem?
		set(entity) {
			super.entity = entity
		}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = item
	}
}