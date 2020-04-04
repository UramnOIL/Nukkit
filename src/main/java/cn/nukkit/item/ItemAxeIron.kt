package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemAxeIron @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.IRON_AXE, meta, count, "Iron Axe") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_IRON

	override val isAxe: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_IRON

	override val attackDamage: Int
		get() = 5
}