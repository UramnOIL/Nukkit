package cn.nukkit.item.enchantment.protection

import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.item.enchantment.Enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentProtectionFall : EnchantmentProtection(Enchantment.Companion.ID_PROTECTION_FALL, "fall", 5, TYPE.FALL) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 5 + (level - 1) * 6
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 10
	}

	override val typeModifier: Double
		get() = 2

	override fun getProtectionFactor(e: EntityDamageEvent): Float {
		val cause = e.cause
		return if (level <= 0 || cause !== DamageCause.FALL) {
			0
		} else (getLevel() * typeModifier)
	}
}