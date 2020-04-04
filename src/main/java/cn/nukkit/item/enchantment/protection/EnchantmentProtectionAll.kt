package cn.nukkit.item.enchantment.protection

import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.item.enchantment.Enchantment

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentProtectionAll : EnchantmentProtection(Enchantment.Companion.ID_PROTECTION_ALL, "all", 10, TYPE.ALL) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 1 + (level - 1) * 11
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 20
	}

	override val typeModifier: Double
		get() = 1

	override fun getProtectionFactor(e: EntityDamageEvent): Float {
		val cause = e.cause
		return if (level <= 0 || cause === DamageCause.VOID || cause === DamageCause.CUSTOM || cause === DamageCause.MAGIC) {
			0
		} else (getLevel() * typeModifier)
	}
}