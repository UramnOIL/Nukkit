package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class PlayerBedEnterEvent(player: Player?, bed: Block) : PlayerEvent(), Cancellable {
	val bed: Block

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.bed = bed
	}
}