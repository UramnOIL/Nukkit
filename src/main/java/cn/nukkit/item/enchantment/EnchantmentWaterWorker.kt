package cn.nukkit.item.enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentWaterWorker : Enchantment(Enchantment.Companion.ID_WATER_WORKER, "waterWorker", 2, EnchantmentType.ARMOR_HEAD) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 1
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 40
	}

	override val maxLevel: Int
		get() = 1
}