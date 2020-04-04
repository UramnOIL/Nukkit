package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * @author Nukkit Project Team
 */
class BlockDiamond : BlockSolid() {
	override val hardness: Double
		get() = 5

	override val resistance: Double
		get() = 30

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val id: Int
		get() = BlockID.Companion.DIAMOND_BLOCK

	override val name: String
		get() = "Diamond Block"

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_IRON) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val color: BlockColor
		get() = BlockColor.DIAMOND_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}