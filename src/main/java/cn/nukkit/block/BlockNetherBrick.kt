package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/12/7 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
open class BlockNetherBrick : BlockSolid() {
	override val name: String
		get() = "Nether Bricks"

	override val id: Int
		get() = BlockID.Companion.NETHER_BRICKS

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 10

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
		get() = BlockColor.NETHERRACK_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}