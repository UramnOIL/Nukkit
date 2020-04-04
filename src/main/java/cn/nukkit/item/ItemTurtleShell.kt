package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * Created by PetteriM1
 */
class ItemTurtleShell @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemArmor(ItemID.Companion.TURTLE_SHELL, meta, count, "Turtle Shell") {
	override val tier: Int
		get() = ItemArmor.Companion.TIER_OTHER

	override val isHelmet: Boolean
		get() = true

	override val armorPoints: Int
		get() = 2

	override val maxDurability: Int
		get() = 276

	override val toughness: Int
		get() = 2
}