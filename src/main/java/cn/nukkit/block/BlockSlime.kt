package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created by Pub4Game on 21.02.2016.
 */
class BlockSlime : BlockSolid() {
	override val hardness: Double
		get() = 0

	override val name: String
		get() = "Slime Block"

	override val id: Int
		get() = BlockID.Companion.SLIME_BLOCK

	override val resistance: Double
		get() = 0

	override val color: BlockColor
		get() = BlockColor.GRASS_BLOCK_COLOR
}