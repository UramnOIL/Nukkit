package cn.nukkit.block

import cn.nukkit.level.generator

/**
 * Created on 2015/12/8 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockPumpkinLit @JvmOverloads constructor(meta: Int = 0) : BlockPumpkin(0) {
	override val name: String
		get() = "Jack o'Lantern"

	override val id: Int
		get() = BlockID.Companion.LIT_PUMPKIN

	override val lightLevel: Int
		get() = 15
}