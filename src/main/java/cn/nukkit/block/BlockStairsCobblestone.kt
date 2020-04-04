package cn.nukkit.block

import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/11/25 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockStairsCobblestone @JvmOverloads constructor(meta: Int = 0) : BlockStairs(meta) {
	override val id: Int
		get() = BlockID.Companion.COBBLESTONE_STAIRS

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 30

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Cobblestone Stairs"

	override val color: BlockColor
		get() = BlockColor.STONE_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}