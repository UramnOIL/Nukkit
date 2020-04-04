package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemHelmetChain @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemArmor(ItemID.Companion.CHAIN_HELMET, meta, count, "Chainmail Helmet") {
	override val tier: Int
		get() = ItemArmor.Companion.TIER_CHAIN

	override val isHelmet: Boolean
		get() = true

	override val armorPoints: Int
		get() = 2

	override val maxDurability: Int
		get() = 166
}