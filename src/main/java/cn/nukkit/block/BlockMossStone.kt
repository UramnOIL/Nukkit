package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator

/**
 * Created on 2015/12/2 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockMossStone : BlockSolid() {
	override val name: String
		get() = "Moss Stone"

	override val id: Int
		get() = BlockID.Companion.MOSS_STONE

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 10

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
}