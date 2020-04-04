package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class BlockSandstone @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.SANDSTONE

	override val hardness: Double
		get() = 0.8

	override val resistance: Double
		get() = 4

	override val name: String
		get() {
			val names = arrayOf(
					"Sandstone",
					"Chiseled Sandstone",
					"Smooth Sandstone",
					""
			)
			return names[this.damage and 0x03]
		}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun toItem(): Item? {
		return ItemBlock(this, this.damage and 0x03)
	}

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val color: BlockColor
		get() = BlockColor.SAND_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	companion object {
		const val NORMAL = 0
		const val CHISELED = 1
		const val SMOOTH = 2
	}
}