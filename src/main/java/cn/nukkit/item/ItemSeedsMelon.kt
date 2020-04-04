package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemSeedsMelon @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.MELON_SEEDS, 0, count, "Melon Seeds") {
	init {
		block = Block[BlockID.MELON_STEM]
	}
}