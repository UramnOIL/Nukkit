package cn.nukkit.event.entity

import cn.nukkit.entity.Entity
import cn.nukkit.entity.mob.EntityCreeper
import cn.nukkit.entity.weather.EntityLightningStrike
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class CreeperPowerEvent(creeper: EntityCreeper?, cause: PowerCause?) : EntityEvent(), Cancellable {
	/**
	 * Gets the cause of the creeper being (un)powered.
	 *
	 * @return A PowerCause value detailing the cause of change in power.
	 */
	val cause: PowerCause?

	/**
	 * Gets the lightning bolt which is striking the Creeper.
	 *
	 * @return The Entity for the lightning bolt which is striking the Creeper
	 */
	var lightning: EntityLightningStrike? = null
		private set

	constructor(creeper: EntityCreeper?, bolt: EntityLightningStrike?, cause: PowerCause?) : this(creeper, cause) {
		lightning = bolt
	}

	override var entity: Entity?
		get() = super.getEntity() as EntityCreeper
		set(entity) {
			super.entity = entity
		}

	/**
	 * An enum to specify the cause of the change in power
	 */
	enum class PowerCause {
		/**
		 * Power change caused by a lightning bolt
		 *
		 *
		 * Powered state: true
		 */
		LIGHTNING,

		/**
		 * Power change caused by something else (probably a plugin)
		 *
		 *
		 * Powered state: true
		 */
		SET_ON,

		/**
		 * Power change caused by something else (probably a plugin)
		 *
		 *
		 * Powered state: false
		 */
		SET_OFF
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.entity = creeper
		this.cause = cause
	}
}