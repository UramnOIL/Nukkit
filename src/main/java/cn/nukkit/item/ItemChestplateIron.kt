package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemChestplateIron @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemArmor(ItemID.Companion.IRON_CHESTPLATE, meta, count, "Iron Chestplate") {
	override val tier: Int
		get() = ItemArmor.Companion.TIER_IRON

	override val isChestplate: Boolean
		get() = true

	override val armorPoints: Int
		get() = 6

	override val maxDurability: Int
		get() = 241
}