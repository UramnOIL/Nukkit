package cn.nukkit.item.enchantment

import cn.nukkit.item.Item

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentEfficiency : Enchantment(Enchantment.Companion.ID_EFFICIENCY, "digging", 10, EnchantmentType.DIGGER) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 1 + (level - 1) * 10
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 50
	}

	override val maxLevel: Int
		get() = 5

	override fun canEnchant(item: Item): Boolean {
		return item.isShears || super.canEnchant(item)
	}
}