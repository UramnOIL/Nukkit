package cn.nukkit.item.enchantment.damage

import cn.nukkit.entity.Entity
import cn.nukkit.item.enchantment.Enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentDamageAll : EnchantmentDamage(Enchantment.Companion.ID_DAMAGE_ALL, "all", 10, TYPE.ALL) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 1 + (level - 1) * 11
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 20
	}

	override val maxEnchantableLevel: Int
		get() = 4

	override fun getDamageBonus(entity: Entity?): Double {
		return if (getLevel() <= 0) {
			0
		} else 0.5 + getLevel() * 0.5
	}
}