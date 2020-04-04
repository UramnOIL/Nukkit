package cn.nukkit.item.enchantment.bow

import cn.nukkit.item.enchantment.Enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentBowKnockback : EnchantmentBow(Enchantment.Companion.ID_BOW_KNOCKBACK, "arrowKnockback", 2) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 12 + (level - 1) * 20
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 50
	}

	override val maxLevel: Int
		get() = 2
}