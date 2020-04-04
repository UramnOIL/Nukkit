package cn.nukkit.block

import cn.nukkit.level.generator

/**
 * author: Angelic47
 * Nukkit Project
 */
class BlockWaterStill : BlockWater {
	constructor() : super(0) {}
	constructor(meta: Int) : super(meta) {}

	override val id: Int
		get() = BlockID.Companion.STILL_WATER

	override val name: String
		get() = "Still Water"

	override fun getBlock(meta: Int): BlockLiquid {
		return Block.Companion.get(BlockID.Companion.STILL_WATER, meta) as BlockLiquid
	}
}