package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemDoorBirch
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

class BlockDoorBirch @JvmOverloads constructor(meta: Int = 0) : BlockDoorWood(meta) {
	override val name: String
		get() = "Birch Door Block"

	override val id: Int
		get() = BlockID.Companion.BIRCH_DOOR_BLOCK

	override fun toItem(): Item? {
		return ItemDoorBirch()
	}

	override val color: BlockColor
		get() = BlockColor.SAND_BLOCK_COLOR
}