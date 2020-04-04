package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created by CreeperFace on 26. 11. 2016.
 */
class BlockDoubleSlabRedSandstone @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.DOUBLE_RED_SANDSTONE_SLAB

	override val resistance: Double
		get() = 30

	override val hardness: Double
		get() = 2

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

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
			return "Double " + names[this.damage and 0x07] + " Slab"
		}

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.RED_SANDSTONE_SLAB), this.damage and 0x07)
	}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					Item.get(Item.RED_SANDSTONE_SLAB, this.damage and 0x07, 2)
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override val color: BlockColor
		get() = when (this.damage and 0x07) {
			0 -> BlockColor.ORANGE_BLOCK_COLOR
			1 -> BlockColor.PURPLE_BLOCK_COLOR
			else -> BlockColor.STONE_BLOCK_COLOR
		}
}