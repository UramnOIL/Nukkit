package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/12/1 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockEndStone : BlockSolid() {
	override val name: String
		get() = "End Stone"

	override val id: Int
		get() = BlockID.Companion.END_STONE

	override val hardness: Double
		get() = 3

	override val resistance: Double
		get() = 45

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override val color: BlockColor
		get() = BlockColor.SAND_BLOCK_COLOR
}