package cn.nukkit.event.block

import cn.nukkit.block.Block
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class BlockFadeEvent(block: Block?, val newState: Block) : BlockEvent(block), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}