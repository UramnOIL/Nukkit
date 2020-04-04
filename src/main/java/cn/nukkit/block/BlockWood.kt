package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class BlockWood @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.WOOD

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 10

	override val name: String
		get() {
			val names = arrayOf(
					"Oak Wood",
					"Spruce Wood",
					"Birch Wood",
					"Jungle Wood"
			)
			return names[this.damage and 0x03]
		}

	override val burnChance: Int
		get() = 5

	override val burnAbility: Int
		get() = 10

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val faces = shortArrayOf(
				0,
				0,
				8,
				8,
				4,
				4
		)
		this.setDamage(this.damage and 0x03 or faces[face.index])
		getLevel().setBlock(block, this, true, true)
		return true
	}

	override fun toItem(): Item? {
		return ItemBlock(this, this.damage and 0x03)
	}

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override val color: BlockColor
		get() = when (damage and 0x07) {
			OAK -> BlockColor.WOOD_BLOCK_COLOR
			SPRUCE -> BlockColor.SPRUCE_BLOCK_COLOR
			BIRCH -> BlockColor.SAND_BLOCK_COLOR
			JUNGLE -> BlockColor.DIRT_BLOCK_COLOR
			else -> BlockColor.WOOD_BLOCK_COLOR
		}

	companion object {
		const val OAK = 0
		const val SPRUCE = 1
		const val BIRCH = 2
		const val JUNGLE = 3
	}
}