package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * Created by CreeperFace on 13.5.2017.
 */
class ItemHopper @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.HOPPER, 0, count, "Hopper") {
	init {
		block = Block[BlockID.HOPPER_BLOCK]
	}
}