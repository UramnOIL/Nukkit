package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/11/25 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockStairsBirch @JvmOverloads constructor(meta: Int = 0) : BlockStairsWood(meta) {
	override val id: Int
		get() = BlockID.Companion.BIRCH_WOOD_STAIRS

	override val name: String
		get() = "Birch Wood Stairs"

	override val color: BlockColor
		get() = BlockColor.SAND_BLOCK_COLOR
}