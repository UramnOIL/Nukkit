package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemHoeWood @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.WOODEN_HOE, meta, count, "Wooden Hoe") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_WOODEN

	override val isHoe: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_WOODEN
}