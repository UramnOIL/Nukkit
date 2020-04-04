package cn.nukkit.item.enchantment.bow

import cn.nukkit.item.enchantment.Enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentBowFlame : EnchantmentBow(Enchantment.Companion.ID_BOW_FLAME, "arrowFire", 2) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 20
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return 50
	}

	override val maxLevel: Int
		get() = 1
}