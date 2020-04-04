package cn.nukkit.event.level

import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.level.format.FullChunk

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ChunkUnloadEvent(chunk: FullChunk) : ChunkEvent(chunk), Cancellable {
	companion object {
		val handlers = HandlerList()
	}
}