package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockDoubleSlab @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.DOUBLE_SLAB

	//todo hardness and residence
	override val hardness: Double
		get() = 2

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

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
			return "Double " + names[this.damage and 0x07] + " Slab"
		}

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					Item.get(Item.SLAB, this.damage and 0x07, 2)
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val color: BlockColor
		get() = when (this.damage and 0x07) {
			WOODEN -> BlockColor.WOOD_BLOCK_COLOR
			COBBLESTONE, BRICK, STONE_BRICK, STONE -> BlockColor.STONE_BLOCK_COLOR
			SANDSTONE -> BlockColor.SAND_BLOCK_COLOR
			QUARTZ -> BlockColor.QUARTZ_BLOCK_COLOR
			NETHER_BRICK -> BlockColor.NETHERRACK_BLOCK_COLOR
			else -> BlockColor.STONE_BLOCK_COLOR
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