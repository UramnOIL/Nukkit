package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.level.Location

class PlayerMoveEvent @JvmOverloads constructor(player: Player?, from: Location, to: Location, resetBlocks: Boolean = true) : PlayerEvent(), Cancellable {
	var from: Location
	var to: Location
	var isResetBlocksAround: Boolean

	override fun setCancelled() {
		super.setCancelled()
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.from = from
		this.to = to
		isResetBlocksAround = resetBlocks
	}
}