package cn.nukkit.block

import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

class BlockStairsPurpur @JvmOverloads constructor(meta: Int = 0) : BlockStairs(meta) {
	override val id: Int
		get() = BlockID.Companion.PURPUR_STAIRS

	override val hardness: Double
		get() = 1.5

	override val resistance: Double
		get() = 30

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Purpur Stairs"

	override val color: BlockColor
		get() = BlockColor.MAGENTA_BLOCK_COLOR
}