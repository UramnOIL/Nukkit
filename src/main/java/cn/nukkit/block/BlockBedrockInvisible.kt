package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created by Pub4Game on 03.01.2016.
 */
class BlockBedrockInvisible : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.INVISIBLE_BEDROCK

	override val name: String
		get() = "Invisible Bedrock"

	override val hardness: Double
		get() = (-1).toDouble()

	override val resistance: Double
		get() = 18000000

	override fun isBreakable(item: Item?): Boolean {
		return false
	}

	override val color: BlockColor
		get() = BlockColor.TRANSPARENT_BLOCK_COLOR

	override fun canBePushed(): Boolean {
		return false
	}

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.AIR))
	}
}