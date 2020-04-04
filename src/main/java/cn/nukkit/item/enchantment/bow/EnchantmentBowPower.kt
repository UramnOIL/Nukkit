package cn.nukkit.item.enchantment.bow

import cn.nukkit.item.enchantment.Enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentBowPower : EnchantmentBow(Enchantment.Companion.ID_BOW_POWER, "arrowDamage", 10) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 1 + (level - 1) * 20
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 15
	}

	override val maxLevel: Int
		get() = 5
}