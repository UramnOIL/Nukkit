package cn.nukkit.event.entity

import cn.nukkit.block.Block
import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.level.Position

/**
 * author: Angelic47
 * Nukkit Project
 */
class EntityExplodeEvent(entity: Entity?, position: Position, blocks: List<Block>, yield: Double) : EntityEvent(), Cancellable {
	val position: Position
	var blockList: List<Block>
	var yield: Double

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = entity
		this.position = position
		blockList = blocks
		this.yield = yield
	}
}