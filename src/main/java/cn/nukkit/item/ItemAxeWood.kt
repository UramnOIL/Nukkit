package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemAxeWood @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.WOODEN_AXE, meta, count, "Wooden Axe") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_WOODEN

	override val isAxe: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_WOODEN

	override val attackDamage: Int
		get() = 3
}