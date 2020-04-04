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
class BlockQuartz @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.QUARTZ_BLOCK

	override val hardness: Double
		get() = 0.8

	override val resistance: Double
		get() = 4

	override val name: String
		get() {
			val names = arrayOf(
					"Quartz Block",
					"Chiseled Quartz Block",
					"Quartz Pillar",
					"Quartz Pillar"
			)
			return names[this.damage and 0x03]
		}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (this.damage != QUARTZ_NORMAL) {
			val faces = shortArrayOf(
					0,
					0,
					8,
					8,
					4,
					4
			)
			this.setDamage(this.damage and 0x03 or faces[face.index])
		}
		getLevel().setBlock(block, this, true, true)
		return true
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
		return ItemBlock(Block.Companion.get(BlockID.Companion.QUARTZ_BLOCK), this.damage and 0x03, 1)
	}

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val color: BlockColor
		get() = BlockColor.QUARTZ_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	companion object {
		const val QUARTZ_NORMAL = 0
		const val QUARTZ_CHISELED = 1
		const val QUARTZ_PILLAR = 2
		const val QUARTZ_PILLAR2 = 3
	}
}