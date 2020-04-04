package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/12/1 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockEmerald : BlockSolid() {
	override val name: String
		get() = "Emerald Block"

	override val id: Int
		get() = BlockID.Companion.EMERALD_BLOCK

	override val hardness: Double
		get() = 5

	override val resistance: Double
		get() = 30

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

	override val color: BlockColor
		get() = BlockColor.EMERALD_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}