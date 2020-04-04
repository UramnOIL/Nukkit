package cn.nukkit.block

import cn.nukkit.level.generator

/**
 * author: Angelic47
 * Nukkit Project
 */
class BlockFurnace @JvmOverloads constructor(meta: Int = 0) : BlockFurnaceBurning(meta) {
	override val name: String
		get() = "Furnace"

	override val id: Int
		get() = BlockID.Companion.FURNACE

	override val lightLevel: Int
		get() = 0

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}