package cn.nukkit.item.enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentWaterWalker : Enchantment(Enchantment.Companion.ID_WATER_WALKER, "waterWalker", 2, EnchantmentType.ARMOR_FEET) {
	override fun getMinEnchantAbility(level: Int): Int {
		return level * 10
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 15
	}

	override val maxLevel: Int
		get() = 3
}