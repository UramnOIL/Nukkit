package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor

class BlockPurpur @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val name: String
		get() {
			val names = arrayOf(
					"Purpur Block",
					"",
					"Purpur Pillar",
					""
			)
			return names[this.damage and 0x03]
		}

	override val id: Int
		get() = BlockID.Companion.PURPUR_BLOCK

	override val hardness: Double
		get() = 1.5

	override val resistance: Double
		get() = 30

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (this.damage != PURPUR_NORMAL) {
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
		return ItemBlock(Block.Companion.get(BlockID.Companion.PURPUR_BLOCK), this.damage and 0x03, 1)
	}

	override val color: BlockColor
		get() = BlockColor.MAGENTA_BLOCK_COLOR

	companion object {
		const val PURPUR_NORMAL = 0
		const val PURPUR_PILLAR = 2
	}
}