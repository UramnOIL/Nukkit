package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created by CreeperFace on 26. 11. 2016.
 */
class BlockSlabStone @JvmOverloads constructor(meta: Int = 0) : BlockSlab(meta, BlockID.Companion.DOUBLE_STONE_SLAB) {
	override val id: Int
		get() = BlockID.Companion.STONE_SLAB

	override val name: String
		get() {
			val names = arrayOf(
					"Stone",
					"Sandstone",
					"Wooden",
					"Cobblestone",
					"Brick",
					"Stone Brick",
					"Quartz",
					"Nether Brick"
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

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val color: BlockColor
		get() = when (this.damage and 0x07) {
			NETHER_BRICK -> BlockColor.NETHERRACK_BLOCK_COLOR
			STONE, COBBLESTONE, BRICK, STONE_BRICK -> BlockColor.STONE_BLOCK_COLOR
			SANDSTONE -> BlockColor.SAND_BLOCK_COLOR
			WOODEN -> BlockColor.WOOD_BLOCK_COLOR
			QUARTZ -> BlockColor.QUARTZ_BLOCK_COLOR
			else -> BlockColor.STONE_BLOCK_COLOR
		}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	companion object {
		const val STONE = 0
		const val SANDSTONE = 1
		const val WOODEN = 2
		const val COBBLESTONE = 3
		const val BRICK = 4
		const val STONE_BRICK = 5
		const val QUARTZ = 6
		const val NETHER_BRICK = 7
	}
}