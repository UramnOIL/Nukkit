package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemAxeGold @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.GOLD_AXE, meta, count, "Gold Axe") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_GOLD

	override val isAxe: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_GOLD

	override val attackDamage: Int
		get() = 3
}