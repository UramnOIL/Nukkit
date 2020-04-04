package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemDoorIron @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.IRON_DOOR, 0, count, "Iron Door") {
	init {
		block = Block[BlockID.IRON_DOOR_BLOCK]
	}
}