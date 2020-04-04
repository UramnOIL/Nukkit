package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemPickaxeWood @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.WOODEN_PICKAXE, meta, count, "Wooden Pickaxe") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_WOODEN

	override val isPickaxe: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_WOODEN

	override val attackDamage: Int
		get() = 2
}