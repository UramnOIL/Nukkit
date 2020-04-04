package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * @author CreeperFace
 */
class ItemRedstoneComparator(meta: Int?, count: Int) : Item(ItemID.Companion.COMPARATOR, meta, count, "Redstone Comparator") {
	@JvmOverloads
	constructor(meta: Int? = 0) : this(0, 1) {
	}

	init {
		block = Block[BlockID.UNPOWERED_COMPARATOR]
	}
}