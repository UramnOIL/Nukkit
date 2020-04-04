package cn.nukkit.event.block

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.blockentity.BlockEntityItemFrame
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.item.Item

/**
 * Created by Pub4Game on 03.07.2016.
 */
class ItemFrameDropItemEvent(val player: Player, block: Block?, val itemFrame: BlockEntityItemFrame, val item: Item) : BlockEvent(block), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}