package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemCarrot @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemEdible(ItemID.Companion.CARROT, 0, count, "Carrot") {
	init {
		block = Block[BlockID.CARROT_BLOCK]
	}
}