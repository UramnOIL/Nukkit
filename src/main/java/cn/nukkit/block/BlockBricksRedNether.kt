package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

class BlockBricksRedNether : BlockNetherBrick() {
	override val name: String
		get() = "Red Nether Bricks"

	override val id: Int
		get() = BlockID.Companion.RED_NETHER_BRICK

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					Item.get(Item.RED_NETHER_BRICK, 0, 1)
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val color: BlockColor
		get() = BlockColor.NETHERRACK_BLOCK_COLOR
}