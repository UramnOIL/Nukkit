package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemLeggingsChain @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemArmor(ItemID.Companion.CHAIN_LEGGINGS, meta, count, "Chain Leggings") {
	override val tier: Int
		get() = ItemArmor.Companion.TIER_CHAIN

	override val isLeggings: Boolean
		get() = true

	override val armorPoints: Int
		get() = 4

	override val maxDurability: Int
		get() = 226
}