package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator

/**
 * author: Angelic47
 * Nukkit Project
 */
class BlockCobblestone : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.COBBLESTONE

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 30

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Cobblestone"

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