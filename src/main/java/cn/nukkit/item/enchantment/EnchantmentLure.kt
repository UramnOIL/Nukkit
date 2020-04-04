package cn.nukkit.item.enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentLure : Enchantment(Enchantment.Companion.ID_LURE, "fishingSpeed", 2, EnchantmentType.FISHING_ROD) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 15 + (level - 1) * 9
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 50
	}

	override val maxLevel: Int
		get() = 3
}