package cn.nukkit.block

import cn.nukkit.level.Level

/**
 * author: Angelic47
 * Nukkit Project
 */
class BlockLavaStill : BlockLava {
	constructor() : super(0) {}
	constructor(meta: Int) : super(meta) {}

	override val id: Int
		get() = BlockID.Companion.STILL_LAVA

	override val name: String
		get() = "Still Lava"

	override fun getBlock(meta: Int): BlockLiquid {
		return Block.Companion.get(BlockID.Companion.STILL_LAVA, meta) as BlockLiquid
	}

	override fun onUpdate(type: Int): Int {
		return if (type != Level.BLOCK_UPDATE_SCHEDULED) {
			super.onUpdate(type)
		} else 0
	}
}