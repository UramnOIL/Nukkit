package cn.nukkit.item.enchantment.damage

import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityArthropod
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.potion.Effect
import java.util.concurrent.ThreadLocalRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EnchantmentDamageArthropods : EnchantmentDamage(Enchantment.Companion.ID_DAMAGE_ARTHROPODS, "arthropods", 5, TYPE.SMITE) {
	override fun getMinEnchantAbility(level: Int): Int {
		return 5 + (level - 1) * 8
	}

	override fun getMaxEnchantAbility(level: Int): Int {
		return getMinEnchantAbility(level) + 20
	}

	override fun getDamageBonus(entity: Entity?): Double {
		return if (entity is EntityArthropod) {
			getLevel() * 2.5
		} else 0
	}

	override fun doPostAttack(attacker: Entity, entity: Entity) {
		if (entity is EntityArthropod) {
			val duration = 20 + ThreadLocalRandom.current().nextInt(10 * level)
			entity.addEffect(Effect.getEffect(Effect.SLOWNESS).setDuration(duration).setAmplifier(3))
		}
	}
}