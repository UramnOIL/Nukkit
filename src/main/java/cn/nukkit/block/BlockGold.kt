package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: Angelic47
 * Nukkit Project
 */
class BlockGold : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.GOLD_BLOCK

	override val name: String
		get() = "Gold Block"

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val hardness: Double
		get() = 3

	override val resistance: Double
		get() = 30

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
		get() = BlockColor.GOLD_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}