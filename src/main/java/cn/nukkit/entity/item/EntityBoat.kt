package cn.nukkit.entity.item

import cn.nukkit.Player
import cn.nukkit.block.BlockWater
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityLiving
import cn.nukkit.entity.data.ByteEntityData
import cn.nukkit.entity.data.FloatEntityData
import cn.nukkit.entity.passive.EntityWaterAnimal
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.vehicle.VehicleMoveEvent
import cn.nukkit.event.vehicle.VehicleUpdateEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBoat
import cn.nukkit.level.GameRule
import cn.nukkit.level.Location
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.particle.SmokeParticle
import cn.nukkit.math.AxisAlignedBB.BBConsumer
import cn.nukkit.math.NukkitMath
import cn.nukkit.math.Vector3
import cn.nukkit.math.Vector3f
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.AnimatePacket
import cn.nukkit.network.protocol.SetEntityLinkPacket
import java.util.*

/**
 * Created by yescallop on 2016/2/13.
 */
class EntityBoat(chunk: FullChunk?, nbt: CompoundTag?) : EntityVehicle(chunk, nbt) {
	protected var sinking = true
	override fun initEntity() {
		super.initEntity()
		dataProperties!!.putByte(DATA_WOOD_ID, namedTag!!.getByte("woodID"))
	}

	override val height: Float
		get() = 0.455f

	override val width: Float
		get() = 1.4f

	protected override val drag: Float
		protected get() = 0.1f

	protected override val gravity: Float
		protected get() = 0.03999999910593033f

	override val baseOffset: Float
		get() = 0.375f

	override fun attack(source: EntityDamageEvent): Boolean {
		return if (invulnerable) {
			false
		} else {
			source.damage = source.damage * 2
			val attack = super.attack(source)
			if (isAlive) {
				performHurtAnimation()
			}
			attack
		}
	}

	override fun close() {
		super.close()
		for (linkedEntity in passengers) {
			linkedEntity!!.riding = null
		}
		val particle = SmokeParticle(this)
		level.addParticle(particle)
	}

	override fun onUpdate(currentTick: Int): Boolean {
		if (closed) {
			return false
		}
		val tickDiff = currentTick - lastUpdate
		if (tickDiff <= 0 && !justCreated) {
			return true
		}
		lastUpdate = currentTick
		var hasUpdate = this.entityBaseTick(tickDiff)
		if (this.isAlive) {
			super.onUpdate(currentTick)
			val waterDiff = waterLevel
			if (!hasControllingPassenger()) {
				if (waterDiff > SINKING_DEPTH && !sinking) {
					sinking = true
				} else if (waterDiff < -SINKING_DEPTH && sinking) {
					sinking = false
				}
				if (waterDiff < -SINKING_DEPTH) {
					motionY = Math.min(0.05, motionY + 0.005)
				} else if (waterDiff < 0 || !sinking) {
					motionY = if (motionY > SINKING_MAX_SPEED) Math.max(motionY - 0.02, SINKING_MAX_SPEED) else motionY + SINKING_SPEED
					//                    this.motionY = this.motionY + SINKING_SPEED > SINKING_MAX_SPEED ? this.motionY - SINKING_SPEED : this.motionY + SINKING_SPEED;
				}
			}
			if (checkObstruction(x, y, z)) {
				hasUpdate = true
			}
			move(motionX, motionY, motionZ)
			var friction = 1 - drag.toDouble()
			if (onGround && (Math.abs(motionX) > 0.00001 || Math.abs(motionZ) > 0.00001)) {
				friction *= getLevel().getBlock(temporalVector!!.setComponents(Math.floor(x) as Int.toDouble(), Math.floor(y - 1) as Int.toDouble(), Math.floor(z).toInt() - 1.toDouble())).frictionFactor
			}
			motionX *= friction
			if (!hasControllingPassenger()) {
				if (waterDiff > SINKING_DEPTH || sinking) {
					motionY = if (waterDiff > 0.5) motionY - gravity else if (motionY - SINKING_SPEED < -SINKING_MAX_SPEED) motionY else motionY - SINKING_SPEED
				}
			}
			motionZ *= friction
			val from = Location(lastX, lastY, lastZ, lastYaw, lastPitch, level)
			val to = Location(x, y, z, yaw, pitch, level)
			getServer().pluginManager.callEvent(VehicleUpdateEvent(this))
			if (from != to) {
				getServer().pluginManager.callEvent(VehicleMoveEvent(this, from, to))
			}

			//TODO: lily pad collision
			updateMovement()
			if (passengers.size < 2) {
				for (entity in level.getCollidingEntities(boundingBox!!.grow(0.20000000298023224, 0.0, 0.20000000298023224), this)) {
					if (entity.riding != null || entity !is EntityLiving || entity is Player || entity is EntityWaterAnimal || isPassenger(entity)) {
						continue
					}
					this.mountEntity(entity)
					if (passengers.size >= 2) {
						break
					}
				}
			}
		}
		return hasUpdate || !onGround || Math.abs(motionX) > 0.00001 || Math.abs(motionY) > 0.00001 || Math.abs(motionZ) > 0.00001
	}

	override fun updatePassengers() {
		updatePassengers(false)
	}

	fun updatePassengers(sendLinks: Boolean) {
		if (passengers.isEmpty()) {
			return
		}
		for (passenger in ArrayList(passengers)) {
			if (!passenger.isAlive) {
				dismountEntity(passenger)
			}
		}
		var ent: Entity
		if (passengers.size == 1) {
			passengers[0].also { ent = it }.seatPosition = getMountedOffset(ent)
			super.updatePassengerPosition(ent)
			if (sendLinks) {
				broadcastLinkPacket(ent, SetEntityLinkPacket.TYPE_RIDE)
			}
		} else if (passengers.size == 2) {
			if (passengers[0].also { ent = it } !is Player) { //swap
				val passenger2 = passengers[1]
				if (passenger2 is Player) {
					passengers[0] = passenger2
					passengers[1] = ent
					ent = passenger2
				}
			}
			ent.seatPosition = getMountedOffset(ent).add(RIDER_PASSENGER_OFFSET)
			super.updatePassengerPosition(ent)
			if (sendLinks) {
				broadcastLinkPacket(ent, SetEntityLinkPacket.TYPE_RIDE)
			}
			passengers[1].also { ent = it }.seatPosition = getMountedOffset(ent).add(PASSENGER_OFFSET)
			super.updatePassengerPosition(ent)
			if (sendLinks) {
				broadcastLinkPacket(ent, SetEntityLinkPacket.TYPE_PASSENGER)
			}
			val yawDiff: Float = if (ent.id % 2 == 0L) 90 else 270.toFloat()
			ent.setRotation(yaw + yawDiff, ent.pitch)
			ent.updateMovement()
		} else {
			for (passenger in passengers) {
				super.updatePassengerPosition(passenger!!)
			}
		}
	}

	val waterLevel: Double
		get() {
			val maxY = boundingBox!!.minY + baseOffset
			val consumer: BBConsumer<Double> = object : BBConsumer<Double?> {
				private var diffY = Double.MAX_VALUE
				override fun accept(x: Int, y: Int, z: Int) {
					val block = level.getBlock(temporalVector!!.setComponents(x.toDouble(), y.toDouble(), z.toDouble()))
					if (block is BlockWater) {
						val level = block.getMaxY()
						diffY = Math.min(maxY - level, diffY)
					}
				}

				override fun get(): Double {
					return diffY
				}
			}
			boundingBox!!.forEach(consumer)
			return consumer.get()
		}

	override fun mountEntity(entity: Entity): Boolean {
		val player = passengers.size >= 1 && passengers[0] is Player
		var mode = SetEntityLinkPacket.TYPE_PASSENGER
		if (!player && (entity is Player || passengers.size == 0)) {
			mode = SetEntityLinkPacket.TYPE_RIDE
		}
		val r = super.mountEntity(entity, mode)
		if (entity.riding != null) {
			updatePassengers(true)
			entity.setDataProperty(ByteEntityData(Entity.Companion.DATA_RIDER_ROTATION_LOCKED, 1))
			entity.setDataProperty(FloatEntityData(Entity.Companion.DATA_RIDER_MAX_ROTATION, 90))
			entity.setDataProperty(FloatEntityData(Entity.Companion.DATA_RIDER_MIN_ROTATION, if (passengers.indexOf(entity) == 1) -90 else 0))

			//            if(entity instanceof Player && mode == SetEntityLinkPacket.TYPE_RIDE){ //TODO: controlling?
//                entity.setDataProperty(new ByteEntityData(DATA_FLAG_WASD_CONTROLLED))
//            }
		}
		return r
	}

	override fun updatePassengerPosition(passenger: Entity) {
		updatePassengers()
	}

	override fun dismountEntity(entity: Entity): Boolean {
		val r = super.dismountEntity(entity)
		updatePassengers()
		entity.setDataProperty(ByteEntityData(Entity.Companion.DATA_RIDER_ROTATION_LOCKED, 0))
		return r
	}

	override fun isControlling(entity: Entity): Boolean {
		return entity is Player && passengers.indexOf(entity) == 0
	}

	override fun onInteract(player: Player, item: Item, clickedPos: Vector3?): Boolean {
		if (passengers.size >= 2) {
			return false
		}
		super.mountEntity(player)
		return super.onInteract(player, item, clickedPos)
	}

	override fun getMountedOffset(entity: Entity?): Vector3f {
		return if (entity is Player) RIDER_PLAYER_OFFSET else RIDER_OFFSET
	}

	fun onPaddle(animation: AnimatePacket.Action, value: Float) {
		val propertyId: Int = if (animation == AnimatePacket.Action.ROW_RIGHT) Entity.Companion.DATA_PADDLE_TIME_RIGHT else Entity.Companion.DATA_PADDLE_TIME_LEFT
		if (getDataPropertyFloat(propertyId) != value) {
			this.setDataProperty(FloatEntityData(propertyId, value))
		}
	}

	override fun applyEntityCollision(entity: Entity) {
		if (riding == null && entity.riding !== this && !entity.passengers.contains(this)) {
			if (!entity.boundingBox!!.intersectsWith(boundingBox!!.grow(0.20000000298023224, -0.1, 0.20000000298023224))) {
				return
			}
			var diffX = entity.x - x
			var diffZ = entity.z - z
			var direction = NukkitMath.getDirection(diffX, diffZ)
			if (direction >= 0.009999999776482582) {
				direction = Math.sqrt(direction)
				diffX /= direction
				diffZ /= direction
				val d3 = Math.min(1 / direction, 1.0)
				diffX *= d3
				diffZ *= d3
				diffX *= 0.05000000074505806
				diffZ *= 0.05000000074505806
				diffX *= 1 + entityCollisionReduction
				if (riding == null) {
					motionX -= diffX
					motionZ -= diffZ
				}
			}
		}
	}

	override fun canPassThrough(): Boolean {
		return false
	}

	override fun kill() {
		super.kill()
		if (level.getGameRules().getBoolean(GameRule.DO_ENTITY_DROPS)) {
			level.dropItem(this, ItemBoat())
		}
	}

	companion object {
		const val networkId = 90
		const val DATA_WOOD_ID = 20
		val RIDER_PLAYER_OFFSET = Vector3f(0, 1.02001f, 0)
		val RIDER_OFFSET = Vector3f(0, -0.2f, 0)
		val PASSENGER_OFFSET = Vector3f(-0.6f)
		val RIDER_PASSENGER_OFFSET = Vector3f(0.2f)
		const val RIDER_INDEX = 0
		const val PASSENGER_INDEX = 1
		const val SINKING_DEPTH = 0.07
		const val SINKING_SPEED = 0.0005
		const val SINKING_MAX_SPEED = 0.005
	}

	init {
		maxHealth = 40
		setHealth(40f)
	}
}