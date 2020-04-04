package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockStairsAcacia @JvmOverloads constructor(meta: Int = 0) : BlockStairsWood(meta) {
	override val id: Int
		get() = BlockID.Companion.ACACIA_WOOD_STAIRS

	override val name: String
		get() = "Acacia Wood Stairs"

	override val color: BlockColor
		get() = BlockColor.ORANGE_BLOCK_COLOR
}