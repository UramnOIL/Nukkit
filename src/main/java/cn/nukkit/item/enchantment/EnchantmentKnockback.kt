package cn.nukkit.item.enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentKnockback : Enchantment(Enchantment.Companion.ID_KNOCKBACK, "knockback", 5, EnchantmentType.SWORD) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 5 + (level - 1) * 20
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 50
	}

	override val maxLevel: Int
		get() = 2
}