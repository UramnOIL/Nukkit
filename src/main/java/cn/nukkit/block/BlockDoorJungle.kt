package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemDoorJungle
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

class BlockDoorJungle @JvmOverloads constructor(meta: Int = 0) : BlockDoorWood(meta) {
	override val name: String
		get() = "Jungle Door Block"

	override val id: Int
		get() = BlockID.Companion.JUNGLE_DOOR_BLOCK

	override fun toItem(): Item? {
		return ItemDoorJungle()
	}

	override val color: BlockColor
		get() = BlockColor.DIRT_BLOCK_COLOR
}