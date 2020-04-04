package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockWood2 @JvmOverloads constructor(meta: Int = 0) : BlockWood(meta) {
	override val id: Int
		get() = BlockID.Companion.WOOD2

	override val name: String
		get() = NAMES[if (this.damage > 2) 0 else this.damage]

	override val color: BlockColor
		get() = when (damage and 0x07) {
			ACACIA -> BlockColor.ORANGE_BLOCK_COLOR
			DARK_OAK -> BlockColor.BROWN_BLOCK_COLOR
			else -> BlockColor.WOOD_BLOCK_COLOR
		}

	companion object {
		const val ACACIA = 0
		const val DARK_OAK = 1
		private val NAMES = arrayOf(
				"Acacia Wood",
				"Dark Oak Wood",
				""
		)
	}
}