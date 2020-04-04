package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemPickaxeStone @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.STONE_PICKAXE, meta, count, "Stone Pickaxe") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_STONE

	override val isPickaxe: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_STONE

	override val attackDamage: Int
		get() = 3
}