package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * @author PikyCZ
 */
class BlockEndGateway : BlockSolid() {
	override val name: String
		get() = "End Gateway"

	override val id: Int
		get() = BlockID.Companion.END_GATEWAY

	override fun canPassThrough(): Boolean {
		return true
	}

	override fun isBreakable(item: Item?): Boolean {
		return false
	}

	override val hardness: Double
		get() = (-1).toDouble()

	override val resistance: Double
		get() = 18000000

	override val lightLevel: Int
		get() = 15

	override fun hasEntityCollision(): Boolean {
		return true
	}

	override val color: BlockColor
		get() = BlockColor.BLACK_BLOCK_COLOR

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.AIR))
	}
}