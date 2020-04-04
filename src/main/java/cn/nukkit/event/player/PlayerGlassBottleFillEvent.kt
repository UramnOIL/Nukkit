package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.event.Cancellable
import cn.nukkit.item.Item

class PlayerGlassBottleFillEvent(player: Player?, target: Block, item: Item) : PlayerEvent(), Cancellable {
	val item: Item
	val block: Block

	init {
		this.player = player
		block = target
		this.item = item.clone()
	}
}