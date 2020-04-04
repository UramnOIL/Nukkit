package cn.nukkit.block

import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/11/25 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockStairsQuartz @JvmOverloads constructor(meta: Int = 0) : BlockStairs(meta) {
	override val id: Int
		get() = BlockID.Companion.QUARTZ_STAIRS

	override val hardness: Double
		get() = 0.8

	override val resistance: Double
		get() = 4

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Quartz Stairs"

	override val color: BlockColor
		get() = BlockColor.QUARTZ_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}