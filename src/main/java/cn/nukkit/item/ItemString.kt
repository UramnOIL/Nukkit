package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemString @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : Item(ItemID.Companion.STRING, meta, count, "String") {
	init {
		block = Block[BlockID.TRIPWIRE_HOOK]
	}
}