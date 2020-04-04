package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemSwordDiamond @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.DIAMOND_SWORD, meta, count, "Diamond Sword") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_DIAMOND

	override val isSword: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_DIAMOND

	override val attackDamage: Int
		get() = 7
}