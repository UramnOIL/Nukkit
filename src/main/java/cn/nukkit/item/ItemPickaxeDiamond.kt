package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemPickaxeDiamond @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.DIAMOND_PICKAXE, meta, count, "Diamond Pickaxe") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_DIAMOND

	override val isPickaxe: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_DIAMOND

	override val attackDamage: Int
		get() = 5
}