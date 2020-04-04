package cn.nukkit.event.block

import cn.nukkit.block.Block
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockSpreadEvent(block: Block?, val source: Block, newState: Block?) : BlockFormEvent(block, newState), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}