package cn.nukkit.event.entity

import cn.nukkit.block.Block
import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * Created on 15-10-26.
 */
class EntityBlockChangeEvent(entity: Entity?, from: Block, to: Block) : EntityEvent(), Cancellable {
	val from: Block
	val to: Block

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = entity
		this.from = from
		this.to = to
	}
}