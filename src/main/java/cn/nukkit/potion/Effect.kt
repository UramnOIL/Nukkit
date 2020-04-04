package cn.nukkit.potion

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.entity.EntityRegainHealthEvent
import cn.nukkit.network.protocol.MobEffectPacket
import cn.nukkit.utils.ServerException

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class Effect @JvmOverloads constructor(val id: Int, val name: String, r: Int, g: Int, b: Int, val isBad: Boolean = false) : Cloneable {
	var duration = 0
		protected set
	var amplifier = 0
		protected set
	protected var color = 0
	var isVisible = true
		protected set
	var isAmbient = false
		protected set

	fun setDuration(ticks: Int): Effect {
		duration = ticks
		return this
	}

	fun setVisible(visible: Boolean): Effect {
		isVisible = visible
		return this
	}

	fun setAmplifier(amplifier: Int): Effect {
		this.amplifier = amplifier
		return this
	}

	fun setAmbient(ambient: Boolean): Effect {
		isAmbient = ambient
		return this
	}

	fun canTick(): Boolean {
		var interval: Int
		when (id) {
			POISON -> {
				return if ((25 shr amplifier).also { interval = it } > 0) {
					duration % interval == 0
				} else true
			}
			WITHER -> {
				return if ((50 shr amplifier).also { interval = it } > 0) {
					duration % interval == 0
				} else true
			}
			REGENERATION -> {
				return if ((40 shr amplifier).also { interval = it } > 0) {
					duration % interval == 0
				} else true
			}
		}
		return false
	}

	fun applyEffect(entity: Entity) {
		when (id) {
			POISON -> if (entity.getHealth() > 1) {
				entity.attack(EntityDamageEvent(entity, DamageCause.MAGIC, 1))
			}
			WITHER -> entity.attack(EntityDamageEvent(entity, DamageCause.MAGIC, 1))
			REGENERATION -> if (entity.getHealth() < entity.maxHealth) {
				entity.heal(EntityRegainHealthEvent(entity, 1, EntityRegainHealthEvent.CAUSE_MAGIC))
			}
		}
	}

	fun getColor(): IntArray {
		return intArrayOf(color shr 16, color shr 8 and 0xff, color and 0xff)
	}

	fun setColor(r: Int, g: Int, b: Int) {
		color = (r and 0xff shl 16) + (g and 0xff shl 8) + (b and 0xff)
	}

	fun add(entity: Entity) {
		val oldEffect = entity.getEffect(id)
		if (oldEffect != null && (Math.abs(amplifier) < Math.abs(oldEffect.amplifier) ||
						Math.abs(amplifier) == Math.abs(oldEffect.amplifier)
						&& duration < oldEffect.duration)) {
			return
		}
		if (entity is Player) {
			val player = entity
			val pk = MobEffectPacket()
			pk.eid = entity.id
			pk.effectId = id
			pk.amplifier = amplifier
			pk.particles = isVisible
			pk.duration = duration
			if (oldEffect != null) {
				pk.eventId = MobEffectPacket.EVENT_MODIFY.toInt()
			} else {
				pk.eventId = MobEffectPacket.EVENT_ADD.toInt()
			}
			player.dataPacket(pk)
			if (id == SPEED) {
				if (oldEffect != null) {
					player.setMovementSpeed(player.movementSpeed / (1 + 0.2f * (oldEffect.amplifier + 1)), false)
				}
				player.setMovementSpeed(player.movementSpeed * (1 + 0.2f * (amplifier + 1)))
			}
			if (id == SLOWNESS) {
				if (oldEffect != null) {
					player.setMovementSpeed(player.movementSpeed / (1 - 0.15f * (oldEffect.amplifier + 1)), false)
				}
				player.setMovementSpeed(player.movementSpeed * (1 - 0.15f * (amplifier + 1)))
			}
		}
		if (id == INVISIBILITY) {
			entity.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_INVISIBLE, true)
			entity.isNameTagVisible = false
		}
		if (id == ABSORPTION) {
			val add = (amplifier + 1) * 4
			if (add > entity.getAbsorption()) entity.setAbsorption(add.toFloat())
		}
	}

	fun remove(entity: Entity) {
		if (entity is Player) {
			val pk = MobEffectPacket()
			pk.eid = entity.id
			pk.effectId = id
			pk.eventId = MobEffectPacket.EVENT_REMOVE.toInt()
			entity.dataPacket(pk)
			if (id == SPEED) {
				entity.setMovementSpeed(entity.movementSpeed / (1 + 0.2f * (amplifier + 1)))
			}
			if (id == SLOWNESS) {
				entity.setMovementSpeed(entity.movementSpeed / (1 - 0.15f * (amplifier + 1)))
			}
		}
		if (id == INVISIBILITY) {
			entity.setDataFlag(Entity.DATA_FLAGS, Entity.DATA_FLAG_INVISIBLE, false)
			entity.isNameTagVisible = true
		}
		if (id == ABSORPTION) {
			entity.setAbsorption(0f)
		}
	}

	public override fun clone(): Effect {
		return try {
			super.clone() as Effect
		} catch (e: CloneNotSupportedException) {
			null
		}
	}

	companion object {
		const val SPEED = 1
		const val SLOWNESS = 2
		const val HASTE = 3
		const val SWIFTNESS = 3
		const val FATIGUE = 4
		const val MINING_FATIGUE = 4
		const val STRENGTH = 5
		const val HEALING = 6
		const val HARMING = 7
		const val JUMP = 8
		const val NAUSEA = 9
		const val CONFUSION = 9
		const val REGENERATION = 10
		const val DAMAGE_RESISTANCE = 11
		const val FIRE_RESISTANCE = 12
		const val WATER_BREATHING = 13
		const val INVISIBILITY = 14
		const val BLINDNESS = 15
		const val NIGHT_VISION = 16
		const val HUNGER = 17
		const val WEAKNESS = 18
		const val POISON = 19
		const val WITHER = 20
		const val HEALTH_BOOST = 21
		const val ABSORPTION = 22
		const val SATURATION = 23
		const val LEVITATION = 24
		const val FATAL_POISON = 25
		const val COUNDIT_POWER = 26
		const val SLOW_FALLING = 27
		protected var effects: Array<Effect?>
		fun init() {
			effects = arrayOfNulls(256)
			effects[SPEED] = Effect(SPEED, "%potion.moveSpeed", 124, 175, 198)
			effects[SLOWNESS] = Effect(SLOWNESS, "%potion.moveSlowdown", 90, 108, 129, true)
			effects[SWIFTNESS] = Effect(SWIFTNESS, "%potion.digSpeed", 217, 192, 67)
			effects[FATIGUE] = Effect(FATIGUE, "%potion.digSlowDown", 74, 66, 23, true)
			effects[STRENGTH] = Effect(STRENGTH, "%potion.damageBoost", 147, 36, 35)
			effects[HEALING] = InstantEffect(HEALING, "%potion.heal", 248, 36, 35)
			effects[HARMING] = InstantEffect(HARMING, "%potion.harm", 67, 10, 9, true)
			effects[JUMP] = Effect(JUMP, "%potion.jump", 34, 255, 76)
			effects[NAUSEA] = Effect(NAUSEA, "%potion.confusion", 85, 29, 74, true)
			effects[REGENERATION] = Effect(REGENERATION, "%potion.regeneration", 205, 92, 171)
			effects[DAMAGE_RESISTANCE] = Effect(DAMAGE_RESISTANCE, "%potion.resistance", 153, 69, 58)
			effects[FIRE_RESISTANCE] = Effect(FIRE_RESISTANCE, "%potion.fireResistance", 228, 154, 58)
			effects[WATER_BREATHING] = Effect(WATER_BREATHING, "%potion.waterBreathing", 46, 82, 153)
			effects[INVISIBILITY] = Effect(INVISIBILITY, "%potion.invisibility", 127, 131, 146)
			effects[BLINDNESS] = Effect(BLINDNESS, "%potion.blindness", 191, 192, 192)
			effects[NIGHT_VISION] = Effect(NIGHT_VISION, "%potion.nightVision", 0, 0, 139)
			effects[HUNGER] = Effect(HUNGER, "%potion.hunger", 46, 139, 87)
			effects[WEAKNESS] = Effect(WEAKNESS, "%potion.weakness", 72, 77, 72, true)
			effects[POISON] = Effect(POISON, "%potion.poison", 78, 147, 49, true)
			effects[WITHER] = Effect(WITHER, "%potion.wither", 53, 42, 39, true)
			effects[HEALTH_BOOST] = Effect(HEALTH_BOOST, "%potion.healthBoost", 248, 125, 35)
			effects[ABSORPTION] = Effect(ABSORPTION, "%potion.absorption", 36, 107, 251)
			effects[SATURATION] = Effect(SATURATION, "%potion.saturation", 255, 0, 255)
			effects[LEVITATION] = Effect(LEVITATION, "%potion.levitation", 206, 255, 255)
			effects[FATAL_POISON] = Effect(FATAL_POISON, "%potion.poison", 78, 147, 49, true)
			effects[COUNDIT_POWER] = Effect(COUNDIT_POWER, "%potion.conduitPower", 29, 194, 209)
			effects[SLOW_FALLING] = Effect(SLOW_FALLING, "%potion.slowFalling", 206, 255, 255)
		}

		fun getEffect(id: Int): Effect {
			return if (id >= 0 && id < effects.size && effects[id] != null) {
				effects[id]!!.clone()
			} else {
				throw ServerException("Effect id: $id not found")
			}
		}

		fun getEffectByName(name: String): Effect {
			var name = name
			name = name.trim { it <= ' ' }.replace(' ', '_').replace("minecraft:", "")
			return try {
				val id = Effect::class.java.getField(name.toUpperCase()).getInt(null)
				getEffect(id)
			} catch (e: Exception) {
				throw RuntimeException(e)
			}
		}
	}

	init {
		setColor(r, g, b)
	}
}