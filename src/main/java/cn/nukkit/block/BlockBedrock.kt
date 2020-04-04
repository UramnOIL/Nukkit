package cn.nukkit.block

import cn.nukkit.inventory.ShulkerBoxInventoryimport
import cn.nukkit.level.generator

cn.nukkit.item.*
import cn.nukkit.nbt.NBTIO

/**
 * author: Angelic47
 * Nukkit Project
 */
class BlockBedrock : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.BEDROCK

	override val hardness: Double
		get() = (-1).toDouble()

	override val resistance: Double
		get() = 18000000

	override val name: String
		get() = "Bedrock"

	override fun isBreakable(item: Item?): Boolean {
		return false
	}

	override fun canBePushed(): Boolean {
		return false
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}