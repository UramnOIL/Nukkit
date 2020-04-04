package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemSeedsPumpkin @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.PUMPKIN_SEEDS, 0, count, "Pumpkin Seeds") {
	init {
		block = Block[BlockID.PUMPKIN_STEM]
	}
}