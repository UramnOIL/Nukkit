package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockBricksStone @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.STONE_BRICKS

	override val hardness: Double
		get() = 1.5

	override val resistance: Double
		get() = 30

	override val name: String
		get() {
			val names = arrayOf(
					"Stone Bricks",
					"Mossy Stone Bricks",
					"Cracked Stone Bricks",
					"Chiseled Stone Bricks"
			)
			return names[this.damage and 0x03]
		}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					Item.get(Item.STONE_BRICKS, this.damage and 0x03, 1)
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	companion object {
		const val NORMAL = 0
		const val MOSSY = 1
		const val CRACKED = 2
		const val CHISELED = 3
	}
}