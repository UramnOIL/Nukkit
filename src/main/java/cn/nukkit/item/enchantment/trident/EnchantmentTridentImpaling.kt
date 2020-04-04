package cn.nukkit.item.enchantment.trident

import cn.nukkit.item.enchantment.Enchantment

class EnchantmentTridentImpaling : EnchantmentTrident(Enchantment.Companion.ID_TRIDENT_IMPALING, "impaling", 2) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 1 + (level - 1) * 10
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 15
	}

	override val maxLevel: Int
		get() = 5
}