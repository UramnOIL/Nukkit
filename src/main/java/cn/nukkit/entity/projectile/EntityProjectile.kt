package cn.nukkit.entity.projectile

import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityLiving
import cn.nukkit.entity.data.LongEntityData
import cn.nukkit.entity.item.EntityEndCrystal
import cn.nukkit.event.entity.*
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.level.MovingObjectPosition
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.NukkitMath
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class EntityProjectile @JvmOverloads constructor(chunk: FullChunk?, nbt: CompoundTag?, shootingEntity: Entity? = null) : Entity(chunk, nbt) {
	var shootingEntity: Entity? = null
	protected fun getDamage(): Double {
		return if (namedTag!!.contains("damage")) namedTag!!.getDouble("damage") else baseDamage
	}

	protected open val baseDamage: Double
		protected get() = 0

	var hadCollision = false
	var closeOnCollide = true
	protected var damage = 0.0
	open val resultDamage: Int
		get() = NukkitMath.ceilDouble(Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ) * getDamage())

	override fun attack(source: EntityDamageEvent): Boolean {
		return source.cause == DamageCause.VOID && super.attack(source)
	}

	open fun onCollideWithEntity(entity: Entity) {
		server!!.pluginManager.callEvent(ProjectileHitEvent(this, MovingObjectPosition.fromEntity(entity)))
		val damage = resultDamage.toFloat()
		val ev: EntityDamageEvent
		ev = if (shootingEntity == null) {
			EntityDamageByEntityEvent(this, entity, DamageCause.PROJECTILE, damage)
		} else {
			EntityDamageByChildEntityEvent(shootingEntity, this, entity, DamageCause.PROJECTILE, damage)
		}
		if (entity.attack(ev)) {
			hadCollision = true
			if (fireTicks > 0) {
				val event = EntityCombustByEntityEvent(this, entity, 5)
				server!!.pluginManager.callEvent(ev)
				if (!event.isCancelled) {
					entity.setOnFire(event.duration)
				}
			}
		}
		if (closeOnCollide) {
			close()
		}
	}

	override fun initEntity() {
		super.initEntity()
		maxHealth = 1
		setHealth(1f)
		if (namedTag!!.contains("Age")) {
			age = namedTag!!.getShort("Age")
		}
	}

	override fun canCollideWith(entity: Entity): Boolean {
		return (entity is EntityLiving || entity is EntityEndCrystal) && !onGround
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag!!.putShort("Age", age)
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
			var movingObjectPosition: MovingObjectPosition? = null
			if (!isCollided) {
				motionY -= this.gravity.toDouble()
			}
			val moveVector = Vector3(x + motionX, y + motionY, z + motionZ)
			val list = getLevel().getCollidingEntities(boundingBox!!.addCoord(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0), this)
			var nearDistance = Int.MAX_VALUE.toDouble()
			var nearEntity: Entity? = null
			for (entity in list) {
				if ( /*!entity.canCollideWith(this) or */entity === shootingEntity && ticksLived < 5) {
					continue
				}
				val axisalignedbb = entity.boundingBox!!.grow(0.3, 0.3, 0.3)
				val ob = axisalignedbb.calculateIntercept(this, moveVector) ?: continue
				val distance = distanceSquared(ob.hitVector)
				if (distance < nearDistance) {
					nearDistance = distance
					nearEntity = entity
				}
			}
			if (nearEntity != null) {
				movingObjectPosition = MovingObjectPosition.fromEntity(nearEntity)
			}
			if (movingObjectPosition != null) {
				if (movingObjectPosition.entityHit != null) {
					onCollideWithEntity(movingObjectPosition.entityHit)
					return true
				}
			}
			move(motionX, motionY, motionZ)
			if (isCollided && !hadCollision) { //collide with block
				hadCollision = true
				motionX = 0.0
				motionY = 0.0
				motionZ = 0.0
				server!!.pluginManager.callEvent(ProjectileHitEvent(this, MovingObjectPosition.fromBlock(this.floorX, this.floorY, this.floorZ, -1, this)))
				return false
			} else if (!isCollided && hadCollision) {
				hadCollision = false
			}
			if (!hadCollision || Math.abs(motionX) > 0.00001 || Math.abs(motionY) > 0.00001 || Math.abs(motionZ) > 0.00001) {
				val f = Math.sqrt(motionX * motionX + motionZ * motionZ)
				yaw = Math.atan2(motionX, motionZ) * 180 / Math.PI
				pitch = Math.atan2(motionY, f) * 180 / Math.PI
				hasUpdate = true
			}
			updateMovement()
		}
		return hasUpdate
	}

	companion object {
		const val DATA_SHOOTER_ID = 17
	}

	init {
		this.shootingEntity = shootingEntity
		if (shootingEntity != null) {
			this.setDataProperty(LongEntityData(DATA_SHOOTER_ID, shootingEntity.id))
		}
	}
}