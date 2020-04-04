package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemShovelStone @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.STONE_SHOVEL, meta, count, "Stone Shovel") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_STONE

	override val isShovel: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_STONE

	override val attackDamage: Int
		get() = 2
}