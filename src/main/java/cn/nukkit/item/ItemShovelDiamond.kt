package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemShovelDiamond @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.DIAMOND_SHOVEL, meta, count, "Diamond Shovel") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_DIAMOND

	override val isShovel: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_DIAMOND

	override val attackDamage: Int
		get() = 4
}