package cn.nukkit.event.block

import cn.nukkit.block.Block
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class LeavesDecayEvent(block: Block?) : BlockEvent(block), Cancellable {
	companion object {
		val handlers = HandlerList()
	}
}