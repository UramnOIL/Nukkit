package cn.nukkit.block

import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockPlanks @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta % 6) {
	override val id: Int
		get() = BlockID.Companion.WOODEN_PLANKS

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 15

	override val burnChance: Int
		get() = 5

	override val burnAbility: Int
		get() = 20

	override val name: String
		get() {
			val names = arrayOf(
					"Oak Wood Planks",
					"Spruce Wood Planks",
					"Birch Wood Planks",
					"Jungle Wood Planks",
					"Acacia Wood Planks",
					"Dark Oak Wood Planks")
			return if (this.damage < 0) "Unknown" else names[this.damage % 6]
		}

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override val color: BlockColor
		get() = when (damage and 0x07) {
			OAK -> BlockColor.WOOD_BLOCK_COLOR
			SPRUCE -> BlockColor.SPRUCE_BLOCK_COLOR
			BIRCH -> BlockColor.SAND_BLOCK_COLOR
			JUNGLE -> BlockColor.DIRT_BLOCK_COLOR
			ACACIA -> BlockColor.ORANGE_BLOCK_COLOR
			DARK_OAK -> BlockColor.BROWN_BLOCK_COLOR
			else -> BlockColor.WOOD_BLOCK_COLOR
		}

	companion object {
		const val OAK = 0
		const val SPRUCE = 1
		const val BIRCH = 2
		const val JUNGLE = 3
		const val ACACIA = 4
		const val DARK_OAK = 5
	}
}