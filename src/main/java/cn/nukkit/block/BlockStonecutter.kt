package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator

class BlockStonecutter : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.STONECUTTER

	override val name: String
		get() = "Stonecutter"

	override val hardness: Double
		get() = 3.5

	override val resistance: Double
		get() = 17.5

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