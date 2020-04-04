package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

class BlockPrismarine @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.PRISMARINE

	override val hardness: Double
		get() = 1.5

	override val resistance: Double
		get() = 30

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = NAMES[if (this.damage > 2) 0 else this.damage]

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

	override val color: BlockColor
		get() = when (damage and 0x07) {
			NORMAL -> BlockColor.CYAN_BLOCK_COLOR
			BRICKS, DARK -> BlockColor.DIAMOND_BLOCK_COLOR
			else -> BlockColor.STONE_BLOCK_COLOR
		}

	companion object {
		const val NORMAL = 0
		const val BRICKS = 1
		const val DARK = 2
		private val NAMES = arrayOf(
				"Prismarine",
				"Prismarine bricks",
				"Dark prismarine"
		)
	}
}