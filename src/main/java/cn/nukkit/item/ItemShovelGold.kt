package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemShovelGold @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.GOLD_SHOVEL, meta, count, "Gold Shovel") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_GOLD

	override val isShovel: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_GOLD

	override val attackDamage: Int
		get() = 1
}