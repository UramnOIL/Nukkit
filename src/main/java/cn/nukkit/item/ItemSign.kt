package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemSign @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.SIGN, 0, count, "Sign") {
	override val maxStackSize: Int
		get() = 16

	init {
		block = Block[BlockID.SIGN_POST]
	}
}