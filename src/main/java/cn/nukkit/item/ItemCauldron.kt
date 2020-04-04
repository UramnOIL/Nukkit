package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * author: CreeperFace
 * Nukkit Project
 */
class ItemCauldron @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.CAULDRON, meta, count, "Cauldron") {
	init {
		block = Block[BlockID.CAULDRON_BLOCK]
	}
}