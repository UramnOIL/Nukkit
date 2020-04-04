package cn.nukkit.block

import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator

/**
 * Created on 2015/11/25 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockStairsStoneBrick @JvmOverloads constructor(meta: Int = 0) : BlockStairs(meta) {
	override val id: Int
		get() = BlockID.Companion.STONE_BRICK_STAIRS

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val hardness: Double
		get() = 1.5

	override val resistance: Double
		get() = 30

	override val name: String
		get() = "Stone Brick Stairs"
}