package cn.nukkit.block

import cn.nukkit.level.generator

/**
 * @author Nukkit Project Team
 */
class BlockMushroomBrown : BlockMushroom {
	constructor() : super() {}
	constructor(meta: Int) : super(0) {}

	override val name: String
		get() = "Brown Mushroom"

	override val id: Int
		get() = BlockID.Companion.BROWN_MUSHROOM

	override val lightLevel: Int
		get() = 1

	protected override val type: Int
		protected get() = 0
}