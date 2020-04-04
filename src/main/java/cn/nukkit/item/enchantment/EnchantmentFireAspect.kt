package cn.nukkit.item.enchantment

import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityCombustByEntityEvent

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentFireAspect : Enchantment(Enchantment.Companion.ID_FIRE_ASPECT, "fire", 2, EnchantmentType.SWORD) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 10 + (level - 1) * 20
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 50
	}

	override val maxLevel: Int
		get() = 2

	override fun doPostAttack(attacker: Entity, entity: Entity) {
		val duration = Math.max(entity.fireTicks / 20, getLevel() * 4)
		val ev = EntityCombustByEntityEvent(attacker, entity, duration)
		if (!ev.isCancelled) entity.setOnFire(ev.duration)
	}
}