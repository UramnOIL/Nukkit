package cn.nukkit.block

import cn.nukkit.level.generator

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockUnknown @JvmOverloads constructor(override val id: Int, meta: Int = 0) : BlockMeta(meta) {

	override val name: String
		get() = "Unknown"

}