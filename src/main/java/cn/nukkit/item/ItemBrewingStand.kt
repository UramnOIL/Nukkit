package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

class ItemBrewingStand @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.BREWING_STAND, 0, count, "Brewing Stand") {
	init {
		block = Block[BlockID.BREWING_STAND_BLOCK]
	}
}