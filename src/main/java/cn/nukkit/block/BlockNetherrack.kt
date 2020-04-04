package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/12/26 by Pub4Game.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockNetherrack : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.NETHERRACK

	override val resistance: Double
		get() = 2

	override val hardness: Double
		get() = 0.4

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Netherrack"

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