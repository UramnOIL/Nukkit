package cn.nukkit.block

import cn.nukkit.level.generator

/**
 * Created on 2015/12/2 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockDandelion @JvmOverloads constructor(meta: Int = 0) : BlockFlower(0) {
	override val name: String
		get() = "Dandelion"

	override val id: Int
		get() = BlockID.Companion.DANDELION

	protected override val uncommonFlower: Block
		protected get() = Block.Companion.get(BlockID.Companion.POPPY)
}