package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.Faceable

/**
 * http://minecraft.gamepedia.com/End_Rod
 *
 * @author PikyCZ
 */
class BlockEndRod @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta), Faceable {
	override val name: String
		get() = "End Rod"

	override val id: Int
		get() = BlockID.Companion.END_ROD

	override val hardness: Double
		get() = 0

	override val resistance: Double
		get() = 0

	override val lightLevel: Int
		get() = 14

	override fun canBePushed(): Boolean {
		return true
	}

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun getMinX(): Double {
		return x + 0.4
	}

	override fun getMinZ(): Double {
		return z + 0.4
	}

	override fun getMaxX(): Double {
		return x + 0.6
	}

	override fun getMaxZ(): Double {
		return z + 0.6
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val faces = intArrayOf(0, 1, 3, 2, 5, 4)
		this.setDamage(faces[if (player != null) face.index else 0])
		getLevel().setBlock(block, this, true, true)
		return true
	}

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}
}