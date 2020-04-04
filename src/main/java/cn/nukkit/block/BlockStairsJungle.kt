package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/11/25 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockStairsJungle @JvmOverloads constructor(meta: Int = 0) : BlockStairsWood(meta) {
	override val id: Int
		get() = BlockID.Companion.JUNGLE_WOOD_STAIRS

	override val name: String
		get() = "Jungle Wood Stairs"

	override val color: BlockColor
		get() = BlockColor.DIRT_BLOCK_COLOR
}