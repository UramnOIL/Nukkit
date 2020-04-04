package cn.nukkit.item.enchantment

import cn.nukkit.item.Item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentSilkTouch : Enchantment(Enchantment.Companion.ID_SILK_TOUCH, "untouching", 1, EnchantmentType.DIGGER) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 15
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 50
	}

	override val maxLevel: Int
		get() = 1

	override fun isCompatibleWith(enchantment: Enchantment): Boolean {
		return super.isCompatibleWith(enchantment) && enchantment.id != Enchantment.Companion.ID_FORTUNE_DIGGING
	}

	override fun canEnchant(item: Item): Boolean {
		return item.isShears || super.canEnchant(item)
	}
}