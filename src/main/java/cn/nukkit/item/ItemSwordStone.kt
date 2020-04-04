package cn.nukkit.item

import cn.nukkit.item.ItemTool

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemSwordStone @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemTool(ItemID.Companion.STONE_SWORD, meta, count, "Stone Sword") {
	override val maxDurability: Int
		get() = ItemTool.Companion.DURABILITY_STONE

	override val isSword: Boolean
		get() = true

	override val tier: Int
		get() = ItemTool.Companion.TIER_STONE

	override val attackDamage: Int
		get() = 5
}