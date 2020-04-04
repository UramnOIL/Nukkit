package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created by CreeperFace on 26. 11. 2016.
 */
class BlockSlabRedSandstone @JvmOverloads constructor(meta: Int = 0) : BlockSlab(meta, BlockID.Companion.DOUBLE_RED_SANDSTONE_SLAB) {
	override val id: Int
		get() = BlockID.Companion.RED_SANDSTONE_SLAB

	override val name: String
		get() {
			val names = arrayOf(
					"Red Sandstone",
					"Purpur",
					"",
					"",
					"",
					"",
					"",
					""
			)
			return (if (this.damage and 0x08 > 0) "Upper " else "") + names[this.damage and 0x07] + " Slab"
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
		return ItemBlock(this, this.damage and 0x07)
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override val color: BlockColor
		get() = BlockColor.ORANGE_BLOCK_COLOR

	companion object {
		const val RED_SANDSTONE = 0
		const val PURPUR = 1 //WHY THIS
	}
}