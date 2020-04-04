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
class BlockSlabWood @JvmOverloads constructor(meta: Int = 0) : BlockSlab(meta, BlockID.Companion.DOUBLE_WOODEN_SLAB) {
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
			return (if (this.damage and 0x08 == 0x08) "Upper " else "") + names[this.damage and 0x07] + " Wooden Slab"
		}

	override val id: Int
		get() = BlockID.Companion.WOOD_SLAB

	override val burnChance: Int
		get() = 5

	override val burnAbility: Int
		get() = 20

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				toItem()
		)
	}

	override fun toItem(): Item? {
		return ItemBlock(this, this.damage and 0x07)
	}

	override val color: BlockColor
		get() = when (damage and 0x07) {
			0 -> BlockColor.WOOD_BLOCK_COLOR
			1 -> BlockColor.SPRUCE_BLOCK_COLOR
			2 -> BlockColor.SAND_BLOCK_COLOR
			3 -> BlockColor.DIRT_BLOCK_COLOR
			4 -> BlockColor.ORANGE_BLOCK_COLOR
			5 -> BlockColor.BROWN_BLOCK_COLOR
			else -> BlockColor.WOOD_BLOCK_COLOR
		}
}