package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/12/2 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockDoubleSlabWood @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.DOUBLE_WOOD_SLAB

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 15

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override val name: String
		get() {
			val names = arrayOf(
					"Oak",
					"Spruce",
					"Birch",
					"Jungle",
					"Acacia",
					"Dark Oak",
					"",
					""
			)
			return "Double " + names[this.damage and 0x07] + " Slab"
		}

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.WOODEN_SLAB), this.damage and 0x07)
	}

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				Item.get(Item.WOOD_SLAB, this.damage and 0x07, 2)
		)
	}

	override val color: BlockColor
		get() = when (this.damage and 0x07) {
			0 -> BlockColor.WOOD_BLOCK_COLOR
			1 -> BlockColor.SPRUCE_BLOCK_COLOR
			2 -> BlockColor.SAND_BLOCK_COLOR
			3 -> BlockColor.DIRT_BLOCK_COLOR
			4 -> BlockColor.ORANGE_BLOCK_COLOR
			5 -> BlockColor.BROWN_BLOCK_COLOR
			else -> BlockColor.WOOD_BLOCK_COLOR
		}
}