package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemSeedsWheat @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.WHEAT_SEEDS, 0, count, "Wheat Seeds") {
	init {
		block = Block[BlockID.WHEAT_BLOCK]
	}
}