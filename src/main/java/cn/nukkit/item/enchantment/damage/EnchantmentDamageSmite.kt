package cn.nukkit.item.enchantment.damage

import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntitySmite
import cn.nukkit.item.enchantment.Enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentDamageSmite : EnchantmentDamage(Enchantment.Companion.ID_DAMAGE_SMITE, "undead", 5, TYPE.SMITE) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 5 + (level - 1) * 8
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 20
	}

	override fun getDamageBonus(entity: Entity?): Double {
		return if (entity is EntitySmite) {
			getLevel() * 2.5
		} else 0
	}
}