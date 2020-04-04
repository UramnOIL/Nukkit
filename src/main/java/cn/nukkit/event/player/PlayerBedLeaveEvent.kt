package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.event.HandlerList

class PlayerBedLeaveEvent(player: Player?, bed: Block) : PlayerEvent() {
	val bed: Block

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.bed = bed
	}
}