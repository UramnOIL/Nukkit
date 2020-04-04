package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BlockTransparentMeta protected constructor(meta: Int = 0) : BlockMeta(meta) {
	override val isTransparent: Boolean
		get() = true

	override val color: BlockColor
		get() = BlockColor.TRANSPARENT_BLOCK_COLOR
}