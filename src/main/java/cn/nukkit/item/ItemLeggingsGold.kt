package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemLeggingsGold @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemArmor(ItemID.Companion.GOLD_LEGGINGS, meta, count, "Gold Leggings") {
	override val tier: Int
		get() = ItemArmor.Companion.TIER_GOLD

	override val isLeggings: Boolean
		get() = true

	override val armorPoints: Int
		get() = 3

	override val maxDurability: Int
		get() = 106
}