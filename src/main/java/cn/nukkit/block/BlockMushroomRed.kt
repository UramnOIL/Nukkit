package cn.nukkit.block

import cn.nukkit.level.generator

/**
 * Created by Pub4Game on 03.01.2015.
 */
class BlockMushroomRed : BlockMushroom {
	constructor() : super() {}
	constructor(meta: Int) : super(0) {}

	override val name: String
		get() = "Red Mushroom"

	override val id: Int
		get() = BlockID.Companion.RED_MUSHROOM

	protected override val type: Int
		protected get() = 1
}