package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.HandlerList
import cn.nukkit.level.Position

class PlayerRespawnEvent @JvmOverloads constructor(player: Player?, position: Position, firstSpawn: Boolean = false) : PlayerEvent() {
	var respawnPosition: Position
	val isFirstSpawn: Boolean

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		respawnPosition = position
		isFirstSpawn = firstSpawn
	}
}