package cn.nukkit.block

import cn.nukkit.level.generator

/**
 * @author CreeperFace
 */
class BlockRedstoneComparatorPowered @JvmOverloads constructor(meta: Int = 0) : BlockRedstoneComparator(meta) {
	override val id: Int
		get() = BlockID.Companion.POWERED_COMPARATOR

	override val name: String
		get() = "Comparator Block Powered"

	override fun getPowered(): BlockRedstoneComparator {
		return this
	}

	init {
		isPowered = true
	}
}