package cn.nukkit.item.enchantment.trident

import cn.nukkit.item.enchantment.Enchantment

class EnchantmentTridentChanneling : EnchantmentTrident(Enchantment.Companion.ID_TRIDENT_CHANNELING, "channeling", 1) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 20
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 50
	}

	override val maxLevel: Int
		get() = 1
}