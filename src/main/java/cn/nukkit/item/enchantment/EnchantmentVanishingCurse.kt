package cn.nukkit.item.enchantment

class EnchantmentVanishingCurse : Enchantment(Enchantment.Companion.ID_VANISHING_CURSE, "vanishingCurse", 1, EnchantmentType.ALL) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 25
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return 50
	}

	override val maxLevel: Int
		get() = 1
}