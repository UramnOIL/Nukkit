package cn.nukkit.event.block

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.item.Item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockPlaceEvent(val player: Player, blockPlace: Block?, val blockReplace: Block, val blockAgainst: Block, val item: Item) : BlockEvent(blockPlace), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}