package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemHoeGold @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.GOLD_HOE, meta, count, "Gold Hoe") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_GOLD

	override val isHoe: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_GOLD
}