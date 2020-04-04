package cn.nukkit.block

import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.BlockFace
import cn.nukkit.math.SimpleAxisAlignedBB

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockWall @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.STONE_WALL

	override val isSolid: Boolean
		get() = false

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 30

	override val name: String
		get() = if (this.damage == 0x01) {
			"Mossy Cobblestone Wall"
		} else "Cobblestone Wall"

	override fun recalculateBoundingBox(): AxisAlignedBB? {
		val north = canConnect(this.getSide(BlockFace.NORTH))
		val south = canConnect(this.getSide(BlockFace.SOUTH))
		val west = canConnect(this.getSide(BlockFace.WEST))
		val east = canConnect(this.getSide(BlockFace.EAST))
		var n: Double = if (north) 0 else 0.25
		var s: Double = if (south) 1 else 0.75
		var w: Double = if (west) 0 else 0.25
		var e: Double = if (east) 1 else 0.75
		if (north && south && !west && !east) {
			w = 0.3125
			e = 0.6875
		} else if (!north && !south && west && east) {
			n = 0.3125
			s = 0.6875
		}
		return SimpleAxisAlignedBB(
				x + w,
				y,
				z + n,
				x + e,
				y + 1.5,
				z + s
		)
	}

	fun canConnect(block: Block?): Boolean {
		return !(block.getId() != BlockID.Companion.COBBLE_WALL && block.getId() != BlockID.Companion.FENCE_GATE) || block!!.isSolid && !block.isTransparent
	}

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	companion object {
		const val NONE_MOSSY_WALL = 0
		const val MOSSY_WALL = 1
	}
}