package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemHelmetDiamond @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemArmor(ItemID.Companion.DIAMOND_HELMET, meta, count, "Diamond Helmet") {
	override val tier: Int
		get() = ItemArmor.Companion.TIER_DIAMOND

	override val isHelmet: Boolean
		get() = true

	override val armorPoints: Int
		get() = 3

	override val maxDurability: Int
		get() = 364

	override val toughness: Int
		get() = 2
}