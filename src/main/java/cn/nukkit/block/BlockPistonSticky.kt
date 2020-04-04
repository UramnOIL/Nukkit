package cn.nukkit.block

import cn.nukkit.level.generator

/**
 * @author CreeperFace
 */
class BlockPistonSticky @JvmOverloads constructor(meta: Int = 0) : BlockPistonBase(meta) {
	override val id: Int
		get() = BlockID.Companion.STICKY_PISTON

	override val name: String
		get() = "Sticky Piston"

	init {
		sticky = true
	}
}