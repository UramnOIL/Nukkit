package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.event.HandlerList
import cn.nukkit.item.Item
import cn.nukkit.math.BlockFace

class PlayerBucketEmptyEvent(who: Player?, blockClicked: Block, blockFace: BlockFace, bucket: Item, itemInHand: Item) : PlayerBucketEvent(who, blockClicked, blockFace, bucket, itemInHand) {
	companion object {
		val handlers = HandlerList()
	}
}