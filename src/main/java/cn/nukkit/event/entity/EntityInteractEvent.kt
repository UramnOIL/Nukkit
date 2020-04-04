package cn.nukkit.event.entity

import cn.nukkit.block.Block
import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * @author CreeperFace
 */
class EntityInteractEvent(entity: Entity?, block: Block) : EntityEvent(), Cancellable {
	val block: Block

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = entity
		this.block = block
	}
}