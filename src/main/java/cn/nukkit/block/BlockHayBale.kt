package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable

/**
 * Created on 2015/11/24 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockHayBale @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta), Faceable {
	override val id: Int
		get() = BlockID.Companion.HAY_BALE

	override val name: String
		get() = "Hay Bale"

	override val hardness: Double
		get() = 0.5

	override val resistance: Double
		get() = 2.5

	override val burnChance: Int
		get() = 60

	override val burnAbility: Int
		get() = 20

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val faces = intArrayOf(
				0,
				0,
				8,
				8,
				4,
				4)
		this.setDamage(this.damage and 0x03 or faces[face.index])
		getLevel().setBlock(block, this, true, true)
		return true
	}

	override val color: BlockColor
		get() = BlockColor.YELLOW_BLOCK_COLOR

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}
}