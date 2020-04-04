package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemShovelIron @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.IRON_SHOVEL, meta, count, "Iron Shovel") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_IRON

	override val isShovel: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_IRON

	override val attackDamage: Int
		get() = 3
}