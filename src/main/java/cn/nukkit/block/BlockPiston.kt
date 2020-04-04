package cn.nukkit.block

import cn.nukkit.level.generator

/**
 * @author CreeperFace
 */
class BlockPiston @JvmOverloads constructor(meta: Int = 0) : BlockPistonBase(meta) {
	override val id: Int
		get() = BlockID.Companion.PISTON

	override val name: String
		get() = "Piston"
}