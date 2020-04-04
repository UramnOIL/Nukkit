package cn.nukkit.event.block

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.item.Item
import cn.nukkit.math.BlockFace

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockBreakEvent(val player: Player?, block: Block, val face: BlockFace?, val item: Item?, drops: Array<Item?>, instaBreak: Boolean, fastBreak: Boolean) : BlockEvent(block), Cancellable {
	var instaBreak = false
	var drops = arrayOfNulls<Item>(0)
	var dropExp = 0
	var isFastBreak = false
		protected set

	@JvmOverloads
	constructor(player: Player?, block: Block, item: Item?, drops: Array<Item?>, instaBreak: Boolean = false, fastBreak: Boolean = false) : this(player, block, null, item, drops, instaBreak, fastBreak) {
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.instaBreak = instaBreak
		this.drops = drops
		isFastBreak = fastBreak
		dropExp = block.dropExp
	}
}