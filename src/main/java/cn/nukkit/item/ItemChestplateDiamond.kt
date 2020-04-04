package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemChestplateDiamond @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemArmor(ItemID.Companion.DIAMOND_CHESTPLATE, meta, count, "Diamond Chestplate") {
	override val tier: Int
		get() = ItemArmor.Companion.TIER_DIAMOND

	override val isChestplate: Boolean
		get() = true

	override val armorPoints: Int
		get() = 8

	override val maxDurability: Int
		get() = 529

	override val toughness: Int
		get() = 2
}