package cn.nukkit.block

import cn.nukkit.level.generator

abstract class BlockMeta protected constructor(override var damage: Int) : Block() {
	override val fullId: Int
		get() = (id shl 4) + damage

}