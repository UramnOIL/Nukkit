package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemCake @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.CAKE, 0, count, "Cake") {
	override val maxStackSize: Int
		get() = 1

	init {
		block = Block[BlockID.CAKE_BLOCK]
	}
}