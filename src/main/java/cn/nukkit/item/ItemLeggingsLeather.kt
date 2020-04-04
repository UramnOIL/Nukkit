package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemLeggingsLeather @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemColorArmor(ItemID.Companion.LEATHER_PANTS, meta, count, "Leather Pants") {
	override fun getTier(): Int {
		return ItemArmor.Companion.TIER_LEATHER
	}

	override fun isLeggings(): Boolean {
		return true
	}

	override fun getArmorPoints(): Int {
		return 2
	}

	override fun getMaxDurability(): Int {
		return 76
	}
}