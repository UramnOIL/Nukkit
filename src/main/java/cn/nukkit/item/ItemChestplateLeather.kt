package cn.nukkit.item

import cn.nukkit.item.ItemArmor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemChestplateLeather @JvmOverloads constructor(meta: Int? = 0, count: Int = 1) : ItemColorArmor(ItemID.Companion.LEATHER_TUNIC, meta, count, "Leather Tunic") {
	override fun getTier(): Int {
		return ItemArmor.Companion.TIER_LEATHER
	}

	override fun isChestplate(): Boolean {
		return true
	}

	override fun getArmorPoints(): Int {
		return 3
	}

	override fun getMaxDurability(): Int {
		return 81
	}
}