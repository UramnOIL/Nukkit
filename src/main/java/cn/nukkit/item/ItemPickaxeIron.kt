package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemPickaxeIron @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.IRON_PICKAXE, meta, count, "Iron Pickaxe") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_IRON

	override val isPickaxe: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_IRON

	override val attackDamage: Int
		get() = 4
}