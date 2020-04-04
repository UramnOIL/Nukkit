package cn.nukkit.event.block

import cn.nukkit.block.Block
import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class BlockIgniteEvent(block: Block?, val source: Block, val entity: Entity, val cause: BlockIgniteCause) : BlockEvent(block), Cancellable {

	enum class BlockIgniteCause {
		EXPLOSION, FIREBALL, FLINT_AND_STEEL, LAVA, LIGHTNING, SPREAD
	}

	companion object {
		val handlers = HandlerList()
	}

}