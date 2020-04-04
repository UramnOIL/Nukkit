package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * @author Nukkit Project Team
 */
class BlockBricks : BlockSolid() {
	override val name: String
		get() = "Bricks"

	override val id: Int
		get() = BlockID.Companion.BRICKS_BLOCK

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 30

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					Item.get(Item.BRICKS_BLOCK, 0, 1)
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val color: BlockColor
		get() = BlockColor.RED_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}