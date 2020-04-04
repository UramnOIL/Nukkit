package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/11/25 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockStairsDarkOak @JvmOverloads constructor(meta: Int = 0) : BlockStairsWood(meta) {
	override val id: Int
		get() = BlockID.Companion.DARK_OAK_WOOD_STAIRS

	override val name: String
		get() = "Dark Oak Wood Stairs"

	override val color: BlockColor
		get() = BlockColor.BROWN_BLOCK_COLOR
}