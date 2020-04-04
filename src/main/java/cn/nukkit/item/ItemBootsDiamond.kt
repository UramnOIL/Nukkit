package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemBootsDiamond @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemArmor(ItemID.Companion.DIAMOND_BOOTS, meta, count, "Diamond Boots") {
	override val tier: Int
		get() = ItemArmor.Companion.TIER_DIAMOND

	override val isBoots: Boolean
		get() = true

	override val armorPoints: Int
		get() = 3

	override val maxDurability: Int
		get() = 430

	override val toughness: Int
		get() = 2
}