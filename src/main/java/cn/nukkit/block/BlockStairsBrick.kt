package cn.nukkit.block

import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/11/25 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockStairsBrick @JvmOverloads constructor(meta: Int = 0) : BlockStairs(meta) {
	override val id: Int
		get() = BlockID.Companion.BRICK_STAIRS

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 30

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Brick Stairs"

	override val color: BlockColor
		get() = BlockColor.RED_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}