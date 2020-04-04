package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemDoorWood @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.WOODEN_DOOR, 0, count, "Oak Door") {
	init {
		block = Block[BlockID.WOODEN_DOOR_BLOCK]
	}
}