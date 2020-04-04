package cn.nukkit.item.enchantment.protection

import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.item.enchantment.Enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentProtectionExplosion : EnchantmentProtection(Enchantment.Companion.ID_PROTECTION_EXPLOSION, "explosion", 2, TYPE.EXPLOSION) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 5 + (level - 1) * 8
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 12
	}

	override val typeModifier: Double
		get() = 2

	override fun getProtectionFactor(e: EntityDamageEvent): Float {
		val cause = e.cause
		return if (level <= 0 || cause !== DamageCause.ENTITY_EXPLOSION && cause !== DamageCause.BLOCK_EXPLOSION) {
			0
		} else (getLevel() * typeModifier)
	}
}