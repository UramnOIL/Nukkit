package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class PlayerChunkRequestEvent(player: Player?, chunkX: Int, chunkZ: Int) : PlayerEvent(), Cancellable {
	val chunkX: Int
	val chunkZ: Int

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.chunkX = chunkX
		this.chunkZ = chunkZ
	}
}