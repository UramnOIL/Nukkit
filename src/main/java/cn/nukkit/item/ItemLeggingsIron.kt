package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemLeggingsIron @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemArmor(ItemID.Companion.IRON_LEGGINGS, meta, count, "Iron Leggings") {
	override val tier: Int
		get() = ItemArmor.Companion.TIER_IRON

	override val isLeggings: Boolean
		get() = true

	override val armorPoints: Int
		get() = 5

	override val maxDurability: Int
		get() = 226
}