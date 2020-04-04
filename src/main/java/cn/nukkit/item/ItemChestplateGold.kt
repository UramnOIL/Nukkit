package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemChestplateGold @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemArmor(ItemID.Companion.GOLD_CHESTPLATE, meta, count, "Gold Chestplate") {
	override val tier: Int
		get() = ItemArmor.Companion.TIER_GOLD

	override val isChestplate: Boolean
		get() = true

	override val armorPoints: Int
		get() = 5

	override val maxDurability: Int
		get() = 113
}