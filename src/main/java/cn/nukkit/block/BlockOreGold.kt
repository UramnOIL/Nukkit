package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockOreGold : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.GOLD_ORE

	override val hardness: Double
		get() = 3

	override val resistance: Double
		get() = 15

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Gold Ore"

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_IRON) {
			arrayOf(
					Item.get(BlockID.Companion.GOLD_ORE)
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}