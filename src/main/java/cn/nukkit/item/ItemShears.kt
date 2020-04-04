package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemShears @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.SHEARS, meta, count, "Shears") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_SHEARS

	override val isShears: Boolean
		get() = true
}