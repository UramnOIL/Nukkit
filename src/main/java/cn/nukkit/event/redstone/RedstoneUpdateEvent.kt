package cn.nukkit.event.redstone

import cn.nukkit.block.Block
import cn.nukkit.event.HandlerList
import cn.nukkit.event.block.BlockUpdateEvent

/**
 * author: Angelic47
 * Nukkit Project
 */
class RedstoneUpdateEvent(source: Block?) : BlockUpdateEvent(source) {
	companion object {
		val handlers = HandlerList()
	}
}