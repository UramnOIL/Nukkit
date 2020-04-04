package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.event.Cancellable
import cn.nukkit.item.Item
import cn.nukkit.math.BlockFace

abstract class PlayerBucketEvent(who: Player?, blockClicked: Block, blockFace: BlockFace, bucket: Item, itemInHand: Item) : PlayerEvent(), Cancellable {
	val blockClicked: Block
	val blockFace: BlockFace

	/**
	 * Returns the bucket used in this event
	 * @return bucket
	 */
	val bucket: Item

	/**
	 * Returns the item in hand after the event
	 * @return item
	 */
	var item: Item

	init {
		player = who
		this.blockClicked = blockClicked
		this.blockFace = blockFace
		item = itemInHand
		this.bucket = bucket
	}
}