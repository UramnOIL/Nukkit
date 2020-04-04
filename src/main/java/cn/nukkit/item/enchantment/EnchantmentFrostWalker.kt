package cn.nukkit.item.enchantment

class EnchantmentFrostWalker : Enchantment(Enchantment.Companion.ID_FROST_WALKER, "frostWalker", 2, EnchantmentType.ARMOR_FEET) {
	override fun getMinEnchantAbility(level: Int): Int {
		return level * 10
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 15
	}

	override val maxLevel: Int
		get() = 2
}