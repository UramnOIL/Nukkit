package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID
import cn.nukkit.utils.DyeColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemBed @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.BED, meta, count, DyeColor.getByWoolData(meta!!).name + " Bed") {
	override val maxStackSize: Int
		get() = 1

	init {
		block = Block[BlockID.BED_BLOCK]
	}
}