package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BlockTransparent : Block() {
	override val isTransparent: Boolean
		get() = true

	override val color: BlockColor
		get() = BlockColor.TRANSPARENT_BLOCK_COLOR
}