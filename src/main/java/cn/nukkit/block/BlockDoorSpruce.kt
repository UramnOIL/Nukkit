package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemDoorSpruce
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

class BlockDoorSpruce @JvmOverloads constructor(meta: Int = 0) : BlockDoorWood(meta) {
	override val name: String
		get() = "Spruce Door Block"

	override val id: Int
		get() = BlockID.Companion.SPRUCE_DOOR_BLOCK

	override fun toItem(): Item? {
		return ItemDoorSpruce()
	}

	override val color: BlockColor
		get() = BlockColor.SPRUCE_BLOCK_COLOR
}