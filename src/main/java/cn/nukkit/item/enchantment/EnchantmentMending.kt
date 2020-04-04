package cn.nukkit.item.enchantment

/**
 * @author Rover656
 */
class EnchantmentMending : Enchantment(Enchantment.Companion.ID_MENDING, "mending", 2, EnchantmentType.ALL) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 25 + (level - 1) * 9
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 50
	}

	override val maxLevel: Int
		get() = 1

	override fun isCompatibleWith(enchantment: Enchantment): Boolean {
		return super.isCompatibleWith(enchantment) && enchantment.id != Enchantment.Companion.ID_BOW_INFINITY
	}
}