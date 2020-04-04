package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemHoeIron @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.IRON_HOE, meta, count, "Iron Hoe") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_IRON

	override val isHoe: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_IRON
}