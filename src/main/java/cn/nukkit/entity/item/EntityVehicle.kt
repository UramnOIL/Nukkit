package cn.nukkit.entity.item

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityInteractable
import cn.nukkit.entity.EntityRideable
import cn.nukkit.entity.data.IntEntityData
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.vehicle.VehicleDamageEvent
import cn.nukkit.event.vehicle.VehicleDestroyEvent
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class EntityVehicle(chunk: FullChunk?, nbt: CompoundTag?) : Entity(chunk, nbt), EntityRideable, EntityInteractable {
	var rollingAmplitude: Int
		get() = getDataPropertyInt(Entity.Companion.DATA_HURT_TIME)
		set(time) {
			this.setDataProperty(IntEntityData(Entity.Companion.DATA_HURT_TIME, time))
		}

	fun getRollingDirection(): Int {
		return getDataPropertyInt(Entity.Companion.DATA_HURT_DIRECTION)
	}

	fun setRollingDirection(direction: Int) {
		this.setDataProperty(IntEntityData(Entity.Companion.DATA_HURT_DIRECTION, direction))
	}

	// false data name (should be DATA_DAMAGE_TAKEN)
	var damage: Int
		get() = getDataPropertyInt(Entity.Companion.DATA_HEALTH) // false data name (should be DATA_DAMAGE_TAKEN)
		set(damage) {
			this.setDataProperty(IntEntityData(Entity.Companion.DATA_HEALTH, damage))
		}

	override val interactButtonText: String
		get() = "Mount"

	override fun canDoInteraction(): Boolean {
		return passengers.isEmpty()
	}

	override fun onUpdate(currentTick: Int): Boolean {
		// The rolling amplitude
		if (rollingAmplitude > 0) {
			rollingAmplitude = rollingAmplitude - 1
		}

		// A killer task
		if (y < -16) {
			kill()
		}
		// Movement code
		updateMovement()
		return true
	}

	protected var rollingDirection = true
	protected fun performHurtAnimation(): Boolean {
		rollingAmplitude = 9
		setRollingDirection(if (rollingDirection) 1 else -1)
		rollingDirection = !rollingDirection
		return true
	}

	override fun attack(source: EntityDamageEvent): Boolean {
		val event = VehicleDamageEvent(this, source.entity, source.finalDamage.toDouble())
		getServer().pluginManager.callEvent(event)
		if (event.isCancelled) {
			return false
		}
		var instantKill = false
		if (source is EntityDamageByEntityEvent) {
			val damager = source.damager
			instantKill = damager is Player && damager.isCreative
		}
		if (instantKill || getHealth() - source.finalDamage < 1) {
			val event2 = VehicleDestroyEvent(this, source.entity)
			getServer().pluginManager.callEvent(event2)
			if (event2.isCancelled) {
				return false
			}
		}
		if (instantKill) {
			source.damage = 1000f
		}
		return super.attack(source)
	}
}