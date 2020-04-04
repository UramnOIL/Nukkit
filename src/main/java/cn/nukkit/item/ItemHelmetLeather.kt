package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemHelmetLeather @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemColorArmor(ItemID.Companion.LEATHER_CAP, meta, count, "Leather Cap") {
	override fun getTier(): Int {
		return ItemArmor.Companion.TIER_LEATHER
	}

	override fun isHelmet(): Boolean {
		return true
	}

	override fun getArmorPoints(): Int {
		return 1
	}

	override fun getMaxDurability(): Int {
		return 56
	}
}