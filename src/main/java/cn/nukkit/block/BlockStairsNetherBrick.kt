package cn.nukkit.block

import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/11/25 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockStairsNetherBrick @JvmOverloads constructor(meta: Int = 0) : BlockStairs(meta) {
	override val id: Int
		get() = BlockID.Companion.NETHER_BRICKS_STAIRS

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 10

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Nether Bricks Stairs"

	override val color: BlockColor
		get() = BlockColor.NETHERRACK_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}