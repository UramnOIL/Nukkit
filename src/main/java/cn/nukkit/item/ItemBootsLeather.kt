package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemBootsLeather @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemColorArmor(ItemID.Companion.LEATHER_BOOTS, meta, count, "Leather Boots") {
	override fun getTier(): Int {
		return ItemArmor.Companion.TIER_LEATHER
	}

	override fun isBoots(): Boolean {
		return true
	}

	override fun getArmorPoints(): Int {
		return 1
	}

	override fun getMaxDurability(): Int {
		return 66
	}
}