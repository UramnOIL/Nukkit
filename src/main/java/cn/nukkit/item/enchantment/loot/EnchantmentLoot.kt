package cn.nukkit.item.enchantment.loot

import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.item.enchantment.EnchantmentType

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class EnchantmentLoot protected constructor(id: Int, name: String, weight: Int, type: EnchantmentType) : Enchantment(id, name, weight, type) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 15 + (level - 1) * 9
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 50
	}

	override val maxLevel: Int
		get() = 3

	override fun isCompatibleWith(enchantment: Enchantment): Boolean {
		return super.isCompatibleWith(enchantment) && enchantment.id != Enchantment.Companion.ID_SILK_TOUCH
	}
}