package cn.nukkit.block

import cn.nukkit.level.generator

/**
 * @author CreeperFace
 */
class BlockRedstoneComparatorUnpowered @JvmOverloads constructor(meta: Int = 0) : BlockRedstoneComparator(meta) {
	override val id: Int
		get() = BlockID.Companion.UNPOWERED_COMPARATOR

	override val name: String
		get() = "Comparator Block Unpowered"

	protected override val unpowered: Block
		protected get() = this
}