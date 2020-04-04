package cn.nukkit.item.enchantment

import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityHumanType
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import java.util.concurrent.ThreadLocalRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentThorns : Enchantment(Enchantment.Companion.ID_THORNS, "thorns", 2, EnchantmentType.ARMOR_TORSO) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 10 + (level - 1) * 20
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 50
	}

	override val maxLevel: Int
		get() = 3

	override fun doPostAttack(attacker: Entity, entity: Entity) {
		if (entity !is EntityHumanType) {
			return
		}
		var thornsLevel = 0
		for (armor in entity.getInventory().armorContents) {
			val thorns = armor!!.getEnchantment(Enchantment.Companion.ID_THORNS)
			if (thorns != null) {
				thornsLevel = Math.max(thorns.getLevel(), thornsLevel)
			}
		}
		val random = ThreadLocalRandom.current()
		if (shouldHit(random, thornsLevel)) {
			attacker.attack(EntityDamageByEntityEvent(entity, attacker, DamageCause.ENTITY_ATTACK, getDamage(random, level), 0f))
		}
	}

	companion object {
		private fun shouldHit(random: ThreadLocalRandom, level: Int): Boolean {
			return level > 0 && random.nextFloat() < 0.15 * level
		}

		private fun getDamage(random: ThreadLocalRandom, level: Int): Int {
			return if (level > 10) level - 10 else random.nextInt(1, 5)
		}
	}
}