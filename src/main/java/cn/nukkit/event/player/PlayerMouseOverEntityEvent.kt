package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.event.HandlerList

class PlayerMouseOverEntityEvent(player: Player?, entity: Entity) : PlayerEvent() {
	val entity: Entity

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.entity = entity
	}
}