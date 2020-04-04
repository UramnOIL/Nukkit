package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

abstract class BlockSolidMeta protected constructor(meta: Int) : BlockMeta(meta) {
	override val isSolid: Boolean
		get() = true

	override val color: BlockColor
		get() = BlockColor.STONE_BLOCK_COLOR
}