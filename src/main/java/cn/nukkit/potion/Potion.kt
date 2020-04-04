package cn.nukkit.potion

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityLiving
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.entity.EntityRegainHealthEvent
import cn.nukkit.event.potion.PotionApplyEvent
import cn.nukkit.potion.Potion
import cn.nukkit.utils.ServerException

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class Potion @JvmOverloads constructor(val id: Int, val level: Int = 1, splash: Boolean = false) : Cloneable {
	var isSplash = false
		protected set
	val effect: Effect?
		get() = getEffect(id, isSplash)

	fun setSplash(splash: Boolean): Potion {
		isSplash = splash
		return this
	}

	@JvmOverloads
	fun applyPotion(entity: Entity, health: Double = 0.5) {
		if (entity !is EntityLiving) {
			return
		}
		var applyEffect = getEffect(id, isSplash) ?: return
		if (entity is Player) {
			if (!entity.isSurvival && !entity.isAdventure && applyEffect.isBad) {
				return
			}
		}
		val event = PotionApplyEvent(this, applyEffect, entity)
		entity.server!!.pluginManager.callEvent(event)
		if (event.isCancelled) {
			return
		}
		applyEffect = event.applyEffect
		when (id) {
			INSTANT_HEALTH, INSTANT_HEALTH_II -> entity.heal(EntityRegainHealthEvent(entity, (health * (4 shl applyEffect.getAmplifier() + 1).toDouble()).toFloat(), EntityRegainHealthEvent.CAUSE_MAGIC))
			HARMING, HARMING_II -> entity.attack(EntityDamageEvent(entity, DamageCause.MAGIC, (health * (6 shl applyEffect.getAmplifier() + 1).toDouble()).toFloat()))
			else -> {
				val duration = ((if (isSplash) health else 1) * applyEffect.getDuration() as Double + 0.5) as Int
				applyEffect.setDuration(duration)
				entity.addEffect(applyEffect)
			}
		}
	}

	public override fun clone(): Potion {
		return try {
			super.clone() as Potion
		} catch (e: CloneNotSupportedException) {
			null
		}
	}

	companion object {
		const val NO_EFFECTS = 0
		const val WATER = 0
		const val MUNDANE = 1
		const val MUNDANE_II = 2
		const val THICK = 3
		const val AWKWARD = 4
		const val NIGHT_VISION = 5
		const val NIGHT_VISION_LONG = 6
		const val INVISIBLE = 7
		const val INVISIBLE_LONG = 8
		const val LEAPING = 9
		const val LEAPING_LONG = 10
		const val LEAPING_II = 11
		const val FIRE_RESISTANCE = 12
		const val FIRE_RESISTANCE_LONG = 13
		const val SPEED = 14
		const val SPEED_LONG = 15
		const val SPEED_II = 16
		const val SLOWNESS = 17
		const val SLOWNESS_LONG = 18
		const val WATER_BREATHING = 19
		const val WATER_BREATHING_LONG = 20
		const val INSTANT_HEALTH = 21
		const val INSTANT_HEALTH_II = 22
		const val HARMING = 23
		const val HARMING_II = 24
		const val POISON = 25
		const val POISON_LONG = 26
		const val POISON_II = 27
		const val REGENERATION = 28
		const val REGENERATION_LONG = 29
		const val REGENERATION_II = 30
		const val STRENGTH = 31
		const val STRENGTH_LONG = 32
		const val STRENGTH_II = 33
		const val WEAKNESS = 34
		const val WEAKNESS_LONG = 35
		const val WITHER_II = 36
		const val TURTLE_MASTER = 37
		const val TURTLE_MASTER_LONG = 38
		const val TURTLE_MASTER_II = 39
		const val SLOW_FALLING = 40
		const val SLOW_FALLING_LONG = 41
		protected var potions: Array<Potion?>
		fun init() {
			potions = arrayOfNulls(256)
			potions[WATER] = Potion(WATER)
			potions[MUNDANE] = Potion(MUNDANE)
			potions[MUNDANE_II] = Potion(MUNDANE_II, 2)
			potions[THICK] = Potion(THICK)
			potions[AWKWARD] = Potion(AWKWARD)
			potions[NIGHT_VISION] = Potion(NIGHT_VISION)
			potions[NIGHT_VISION_LONG] = Potion(NIGHT_VISION_LONG)
			potions[INVISIBLE] = Potion(INVISIBLE)
			potions[INVISIBLE_LONG] = Potion(INVISIBLE_LONG)
			potions[LEAPING] = Potion(LEAPING)
			potions[LEAPING_LONG] = Potion(LEAPING_LONG)
			potions[LEAPING_II] = Potion(LEAPING_II, 2)
			potions[FIRE_RESISTANCE] = Potion(FIRE_RESISTANCE)
			potions[FIRE_RESISTANCE_LONG] = Potion(FIRE_RESISTANCE_LONG)
			potions[SPEED] = Potion(SPEED)
			potions[SPEED_LONG] = Potion(SPEED_LONG)
			potions[SPEED_II] = Potion(SPEED_II, 2)
			potions[SLOWNESS] = Potion(SLOWNESS)
			potions[SLOWNESS_LONG] = Potion(SLOWNESS_LONG)
			potions[WATER_BREATHING] = Potion(WATER_BREATHING)
			potions[WATER_BREATHING_LONG] = Potion(WATER_BREATHING_LONG)
			potions[INSTANT_HEALTH] = Potion(INSTANT_HEALTH)
			potions[INSTANT_HEALTH_II] = Potion(INSTANT_HEALTH_II, 2)
			potions[HARMING] = Potion(HARMING)
			potions[HARMING_II] = Potion(HARMING_II, 2)
			potions[POISON] = Potion(POISON)
			potions[POISON_LONG] = Potion(POISON_LONG)
			potions[POISON_II] = Potion(POISON_II, 2)
			potions[REGENERATION] = Potion(REGENERATION)
			potions[REGENERATION_LONG] = Potion(REGENERATION_LONG)
			potions[REGENERATION_II] = Potion(REGENERATION_II, 2)
			potions[STRENGTH] = Potion(STRENGTH)
			potions[STRENGTH_LONG] = Potion(STRENGTH_LONG)
			potions[STRENGTH_II] = Potion(STRENGTH_II, 2)
			potions[WEAKNESS] = Potion(WEAKNESS)
			potions[WEAKNESS_LONG] = Potion(WEAKNESS_LONG)
			potions[WITHER_II] = Potion(WITHER_II, 2)
			potions[TURTLE_MASTER] = Potion(TURTLE_MASTER)
			potions[TURTLE_MASTER_LONG] = Potion(TURTLE_MASTER_LONG)
			potions[TURTLE_MASTER_II] = Potion(TURTLE_MASTER_II, 2)
			potions[SLOW_FALLING] = Potion(SLOW_FALLING)
			potions[SLOW_FALLING_LONG] = Potion(SLOW_FALLING_LONG)
		}

		fun getPotion(id: Int): Potion {
			return if (id >= 0 && id < potions.size && potions[id] != null) {
				potions[id]!!.clone()
			} else {
				throw ServerException("Effect id: $id not found")
			}
		}

		fun getPotionByName(name: String): Potion {
			return try {
				val id = Potion::class.java.getField(name.toUpperCase()).getByte(null)
				getPotion(id.toInt())
			} catch (e: Exception) {
				throw RuntimeException(e)
			}
		}

		fun getEffect(potionType: Int, isSplash: Boolean): Effect? {
			val effect: Effect
			effect = when (potionType) {
				NO_EFFECTS, MUNDANE, MUNDANE_II, THICK, AWKWARD -> return null
				NIGHT_VISION, NIGHT_VISION_LONG -> Effect.Companion.getEffect(Effect.Companion.NIGHT_VISION)
				INVISIBLE, INVISIBLE_LONG -> Effect.Companion.getEffect(Effect.Companion.INVISIBILITY)
				LEAPING, LEAPING_LONG, LEAPING_II -> Effect.Companion.getEffect(Effect.Companion.JUMP)
				FIRE_RESISTANCE, FIRE_RESISTANCE_LONG -> Effect.Companion.getEffect(Effect.Companion.FIRE_RESISTANCE)
				SPEED, SPEED_LONG, SPEED_II -> Effect.Companion.getEffect(Effect.Companion.SPEED)
				SLOWNESS, SLOWNESS_LONG -> Effect.Companion.getEffect(Effect.Companion.SLOWNESS)
				WATER_BREATHING, WATER_BREATHING_LONG -> Effect.Companion.getEffect(Effect.Companion.WATER_BREATHING)
				INSTANT_HEALTH, INSTANT_HEALTH_II -> return Effect.Companion.getEffect(Effect.Companion.HEALING)
				HARMING, HARMING_II -> return Effect.Companion.getEffect(Effect.Companion.HARMING)
				POISON, POISON_LONG, POISON_II -> Effect.Companion.getEffect(Effect.Companion.POISON)
				REGENERATION, REGENERATION_LONG, REGENERATION_II -> Effect.Companion.getEffect(Effect.Companion.REGENERATION)
				STRENGTH, STRENGTH_LONG, STRENGTH_II -> Effect.Companion.getEffect(Effect.Companion.STRENGTH)
				WEAKNESS, WEAKNESS_LONG -> Effect.Companion.getEffect(Effect.Companion.WEAKNESS)
				WITHER_II -> Effect.Companion.getEffect(Effect.Companion.WITHER)
				else -> return null
			}
			if (getLevel(potionType) > 1) {
				effect.setAmplifier(1)
			}
			if (!isInstant(potionType)) {
				effect.setDuration(20 * getApplySeconds(potionType, isSplash))
			}
			return effect
		}

		fun getLevel(potionType: Int): Int {
			return when (potionType) {
				MUNDANE_II, LEAPING_II, SPEED_II, INSTANT_HEALTH_II, HARMING_II, POISON_II, REGENERATION_II, STRENGTH_II, WITHER_II, TURTLE_MASTER_II -> 2
				else -> 1
			}
		}

		fun isInstant(potionType: Int): Boolean {
			return when (potionType) {
				INSTANT_HEALTH, INSTANT_HEALTH_II, HARMING, HARMING_II -> true
				else -> false
			}
		}

		fun getApplySeconds(potionType: Int, isSplash: Boolean): Int {
			return if (isSplash) {
				when (potionType) {
					NO_EFFECTS -> 0
					MUNDANE -> 0
					MUNDANE_II -> 0
					THICK -> 0
					AWKWARD -> 0
					NIGHT_VISION -> 135
					NIGHT_VISION_LONG -> 360
					INVISIBLE -> 135
					INVISIBLE_LONG -> 360
					LEAPING -> 135
					LEAPING_LONG -> 360
					LEAPING_II -> 67
					FIRE_RESISTANCE -> 135
					FIRE_RESISTANCE_LONG -> 360
					SPEED -> 135
					SPEED_LONG -> 360
					SPEED_II -> 67
					SLOWNESS -> 67
					SLOWNESS_LONG -> 180
					WATER_BREATHING -> 135
					WATER_BREATHING_LONG -> 360
					INSTANT_HEALTH -> 0
					INSTANT_HEALTH_II -> 0
					HARMING -> 0
					HARMING_II -> 0
					POISON -> 33
					POISON_LONG -> 90
					POISON_II -> 16
					REGENERATION -> 33
					REGENERATION_LONG -> 90
					REGENERATION_II -> 16
					STRENGTH -> 135
					STRENGTH_LONG -> 360
					STRENGTH_II -> 67
					WEAKNESS -> 67
					WEAKNESS_LONG -> 180
					WITHER_II -> 30
					else -> 0
				}
			} else {
				when (potionType) {
					NO_EFFECTS -> 0
					MUNDANE -> 0
					MUNDANE_II -> 0
					THICK -> 0
					AWKWARD -> 0
					NIGHT_VISION -> 180
					NIGHT_VISION_LONG -> 480
					INVISIBLE -> 180
					INVISIBLE_LONG -> 480
					LEAPING -> 180
					LEAPING_LONG -> 480
					LEAPING_II -> 90
					FIRE_RESISTANCE -> 180
					FIRE_RESISTANCE_LONG -> 480
					SPEED -> 180
					SPEED_LONG -> 480
					SPEED_II -> 480
					SLOWNESS -> 90
					SLOWNESS_LONG -> 240
					WATER_BREATHING -> 180
					WATER_BREATHING_LONG -> 480
					INSTANT_HEALTH -> 0
					INSTANT_HEALTH_II -> 0
					HARMING -> 0
					HARMING_II -> 0
					POISON -> 45
					POISON_LONG -> 120
					POISON_II -> 22
					REGENERATION -> 45
					REGENERATION_LONG -> 120
					REGENERATION_II -> 22
					STRENGTH -> 180
					STRENGTH_LONG -> 480
					STRENGTH_II -> 90
					WEAKNESS -> 90
					WEAKNESS_LONG -> 240
					WITHER_II -> 30
					else -> 0
				}
			}
		}
	}

	init {
		isSplash = splash
	}
}