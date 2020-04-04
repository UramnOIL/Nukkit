package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

class ItemDoorDarkOak @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.DARK_OAK_DOOR, 0, count, "Dark Oak Door") {
	init {
		block = Block[BlockID.DARK_OAK_DOOR_BLOCK]
	}
}