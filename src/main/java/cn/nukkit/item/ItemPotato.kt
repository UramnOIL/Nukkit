package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class ItemPotato : ItemEdible {
	@JvmOverloads
	constructor(meta: Int? = 0, count: Int = 1) : super(ItemID.Companion.POTATO, meta, count, "Potato") {
		block = Block[BlockID.POTATO_BLOCK]
	}

	protected constructor(id: Int, meta: Int?, count: Int, name: String) : super(id, meta, count, name) {}
}