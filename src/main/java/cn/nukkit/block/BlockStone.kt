package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockStone @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.STONE

	override val hardness: Double
		get() = 1.5

	override val resistance: Double
		get() = 10

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() {
			val names = arrayOf(
					"Stone",
					"Granite",
					"Polished Granite",
					"Diorite",
					"Polished Diorite",
					"Andesite",
					"Polished Andesite",
					"Unknown Stone"
			)
			return names[this.damage and 0x07]
		}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					Item.get(if (this.damage == 0) Item.COBBLESTONE else Item.STONE, this.damage, 1)
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun canSilkTouch(): Boolean {
		return true
	}

	companion object {
		const val NORMAL = 0
		const val GRANITE = 1
		const val POLISHED_GRANITE = 2
		const val DIORITE = 3
		const val POLISHED_DIORITE = 4
		const val ANDESITE = 5
		const val POLISHED_ANDESITE = 6
	}
}