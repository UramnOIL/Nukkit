package cn.nukkit.item.enchantment.protection

import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.item.enchantment.Enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentProtectionProjectile : EnchantmentProtection(Enchantment.Companion.ID_PROTECTION_PROJECTILE, "projectile", 5, TYPE.PROJECTILE) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 3 + (level - 1) * 6
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 15
	}

	override val typeModifier: Double
		get() = 3

	override fun getProtectionFactor(e: EntityDamageEvent): Float {
		val cause = e.cause
		return if (level <= 0 || cause !== DamageCause.PROJECTILE) {
			0
		} else (getLevel() * typeModifier)
	}
}