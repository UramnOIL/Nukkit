package cn.nukkit.event.level

import cn.nukkit.event.HandlerList
import cn.nukkit.level.format.FullChunk

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ChunkLoadEvent(chunk: FullChunk, val isNewChunk: Boolean) : ChunkEvent(chunk) {

	companion object {
		val handlers = HandlerList()
	}

}