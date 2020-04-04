package cn.nukkit.event.entity

import cn.nukkit.entity.Entity
import cn.nukkit.potion.Effect

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class EntityDamageByEntityEvent : EntityDamageEvent {
	val damager: Entity
	var knockBack: Float

	@JvmOverloads
	constructor(damager: Entity, entity: Entity, cause: DamageCause?, damage: Float, knockBack: Float = 0.3f) : super(entity, cause, damage) {
		this.damager = damager
		this.knockBack = knockBack
		addAttackerModifiers(damager)
	}

	@JvmOverloads
	constructor(damager: Entity, entity: Entity, cause: DamageCause?, modifiers: Map<DamageModifier?, Float?>?, knockBack: Float = 0.3f) : super(entity, cause, modifiers) {
		this.damager = damager
		this.knockBack = knockBack
		addAttackerModifiers(damager)
	}

	protected fun addAttackerModifiers(damager: Entity) {
		if (damager.hasEffect(Effect.STRENGTH)) {
			this.setDamage((this.getDamage(DamageModifier.BASE) * 0.3 * (damager.getEffect(Effect.STRENGTH)!!.amplifier + 1)).toFloat(), DamageModifier.STRENGTH)
		}
		if (damager.hasEffect(Effect.WEAKNESS)) {
			this.setDamage((-(this.getDamage(DamageModifier.BASE) * 0.2 * (damager.getEffect(Effect.WEAKNESS)!!.amplifier + 1))).toFloat(), DamageModifier.WEAKNESS)
		}
	}

}