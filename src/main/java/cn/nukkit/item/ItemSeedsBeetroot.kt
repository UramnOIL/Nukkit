package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemSeedsBeetroot @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.BEETROOT_SEEDS, 0, count, "Beetroot Seeds") {
	init {
		block = Block[BlockID.BEETROOT_BLOCK]
	}
}