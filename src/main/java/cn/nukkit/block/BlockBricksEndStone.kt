package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

class BlockBricksEndStone : BlockSolid() {
	override val name: String
		get() = "End Stone Bricks"

	override val id: Int
		get() = BlockID.Companion.END_BRICKS

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val hardness: Double
		get() = 0.8

	override val resistance: Double
		get() = 4

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					Item.get(Item.END_BRICKS, 0, 1)
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val color: BlockColor
		get() = BlockColor.SAND_BLOCK_COLOR
}