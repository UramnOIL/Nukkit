package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemChestplateChain @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemArmor(ItemID.Companion.CHAIN_CHESTPLATE, meta, count, "Chain Chestplate") {
	override val tier: Int
		get() = ItemArmor.Companion.TIER_CHAIN

	override val isChestplate: Boolean
		get() = true

	override val armorPoints: Int
		get() = 5

	override val maxDurability: Int
		get() = 241
}