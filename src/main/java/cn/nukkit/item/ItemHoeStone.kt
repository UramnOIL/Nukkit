package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemHoeStone @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.STONE_HOE, meta, count, "Stone Hoe") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_STONE

	override val isHoe: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_STONE
}