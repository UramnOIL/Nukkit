package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemLeggingsDiamond @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemArmor(ItemID.Companion.DIAMOND_LEGGINGS, meta, count, "Diamond Leggings") {
	override val isLeggings: Boolean
		get() = true

	override val tier: Int
		get() = ItemArmor.Companion.TIER_DIAMOND

	override val armorPoints: Int
		get() = 6

	override val maxDurability: Int
		get() = 496

	override val toughness: Int
		get() = 2
}