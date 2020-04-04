package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: Angelic47
 * Nukkit Project
 */
class BlockIron : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.IRON_BLOCK

	override val name: String
		get() = "Iron Block"

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val hardness: Double
		get() = 5

	override val resistance: Double
		get() = 10

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_STONE) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val color: BlockColor
		get() = BlockColor.IRON_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}