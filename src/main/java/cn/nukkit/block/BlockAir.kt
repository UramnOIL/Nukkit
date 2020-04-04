package cn.nukkit.block

import cn.nukkit.inventory.ShulkerBoxInventoryimport
import cn.nukkit.level.generator
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.utils.BlockColor

cn.nukkit.item.*
import cn.nukkit.nbt.NBTIO

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockAir : BlockTransparent() {
	override val id: Int
		get() = BlockID.Companion.AIR

	override val name: String
		get() = "Air"

	override fun canPassThrough(): Boolean {
		return true
	}

	override fun isBreakable(item: Item?): Boolean {
		return false
	}

	override fun canBeFlowedInto(): Boolean {
		return true
	}

	override fun canBePlaced(): Boolean {
		return false
	}

	override fun canBeReplaced(): Boolean {
		return true
	}

	override val isSolid: Boolean
		get() = false

	override val boundingBox: AxisAlignedBB?
		get() = null

	override val hardness: Double
		get() = 0

	override val resistance: Double
		get() = 0

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR
}