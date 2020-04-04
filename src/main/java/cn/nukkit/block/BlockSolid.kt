package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BlockSolid protected constructor() : Block() {
	override val isSolid: Boolean
		get() = true

	override val color: BlockColor
		get() = BlockColor.STONE_BLOCK_COLOR
}