package cn.nukkit.item.enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentWaterBreath : Enchantment(Enchantment.Companion.ID_WATER_BREATHING, "oxygen", 2, EnchantmentType.ARMOR_HEAD) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 10 + (level - 1) * 20
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 50
	}

	override val maxLevel: Int
		get() = 3
}