package cn.nukkit.item.enchantment.trident

import cn.nukkit.item.enchantment.Enchantment

class EnchantmentTridentLoyalty : EnchantmentTrident(Enchantment.Companion.ID_TRIDENT_LOYALTY, "loyalty", 5) {
	override fun getMinEnchantAbility(level: Int): Int {
		return level * 10
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 15
	}

	override val maxLevel: Int
		get() = 3
}