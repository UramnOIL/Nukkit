package cn.nukkit.entity.item

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import it.unimi.dsi.fastutil.ints.IntArrayList

/**
 * Created on 2015/12/26 by xtypr.
 * Package cn.nukkit.entity in project Nukkit .
 */
class EntityXPOrb(chunk: FullChunk?, nbt: CompoundTag?) : Entity(chunk, nbt) {

	override val width: Float
		get() = 0.25f

	override val length: Float
		get() = 0.25f

	override val height: Float
		get() = 0.25f

	protected override val gravity: Float
		protected get() = 0.04f

	protected override val drag: Float
		protected get() = 0.02f

	override fun canCollide(): Boolean {
		return false
	}

	private override var age = 0
	var pickupDelay = 0
	private var exp = 0
	var closestPlayer: Player? = null
	override fun initEntity() {
		super.initEntity()
		maxHealth = 5
		setHealth(5f)
		if (namedTag!!.contains("Health")) {
			setHealth(namedTag!!.getShort("Health").toFloat())
		}
		if (namedTag!!.contains("Age")) {
			this.age = namedTag!!.getShort("Age")
		}
		if (namedTag!!.contains("PickupDelay")) {
			pickupDelay = namedTag!!.getShort("PickupDelay")
		}
		if (namedTag!!.contains("Value")) {
			exp = namedTag!!.getShort("Value")
		}
		if (exp <= 0) {
			exp = 1
		}
		dataProperties!!.putInt(Entity.Companion.DATA_EXPERIENCE_VALUE, exp)

		//call event item spawn event
	}

	override fun attack(source: EntityDamageEvent): Boolean {
		return (source.cause == DamageCause.VOID || source.cause == DamageCause.FIRE_TICK || (source.cause == DamageCause.ENTITY_EXPLOSION ||
				source.cause == DamageCause.BLOCK_EXPLOSION) &&
				!this.isInsideOfWater) && super.attack(source)
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
		var hasUpdate = entityBaseTick(tickDiff)
		if (this.isAlive) {
			if (pickupDelay > 0 && pickupDelay < 32767) { //Infinite delay
				pickupDelay -= tickDiff
				if (pickupDelay < 0) {
					pickupDelay = 0
				}
			} else {
				for (entity in level.getCollidingEntities(boundingBox, this)) {
					if (entity is Player) {
						if (entity.pickupEntity(this, false)) {
							return true
						}
					}
				}
			}
			motionY -= gravity.toDouble()
			if (checkObstruction(x, y, z)) {
				hasUpdate = true
			}
			if (closestPlayer == null || closestPlayer!!.distanceSquared(this) > 64.0) {
				for (p in level.players.values) {
					if (!p.isSpectator && p.distance(this) <= 8) {
						closestPlayer = p
						break
					}
				}
			}
			if (closestPlayer != null && closestPlayer!!.isSpectator) {
				closestPlayer = null
			}
			if (closestPlayer != null) {
				val dX = (closestPlayer!!.x - x) / 8.0
				val dY = (closestPlayer!!.y + closestPlayer.getEyeHeight() as Double / 2.0 - y) / 8.0
				val dZ = (closestPlayer!!.z - z) / 8.0
				val d = Math.sqrt(dX * dX + dY * dY + dZ * dZ)
				var diff = 1.0 - d
				if (diff > 0.0) {
					diff = diff * diff
					motionX += dX / d * diff * 0.1
					motionY += dY / d * diff * 0.1
					motionZ += dZ / d * diff * 0.1
				}
			}
			move(motionX, motionY, motionZ)
			var friction = 1.0 - drag
			if (onGround && (Math.abs(motionX) > 0.00001 || Math.abs(motionZ) > 0.00001)) {
				friction = getLevel().getBlock(temporalVector!!.setComponents(Math.floor(x) as Int.toDouble(), Math.floor(y - 1) as Int.toDouble(), Math.floor(z).toInt() - 1.toDouble())).frictionFactor * friction
			}
			motionX *= friction
			motionY *= 1 - drag.toDouble()
			motionZ *= friction
			if (onGround) {
				motionY *= -0.5
			}
			updateMovement()
			if (this.age > 6000) {
				kill()
				hasUpdate = true
			}
		}
		return hasUpdate || !onGround || Math.abs(motionX) > 0.00001 || Math.abs(motionY) > 0.00001 || Math.abs(motionZ) > 0.00001
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag!!.putShort("Health", getHealth().toInt())
		namedTag!!.putShort("Age", age)
		namedTag!!.putShort("PickupDelay", pickupDelay)
		namedTag!!.putShort("Value", exp)
	}

	fun getExp(): Int {
		return exp
	}

	fun setExp(exp: Int) {
		require(exp > 0) { "XP amount must be greater than 0, got $exp" }
		this.exp = exp
	}

	override fun canCollideWith(entity: Entity): Boolean {
		return false
	}

	companion object {
		const val networkId = 69

		/**
		 * Split sizes used for dropping experience orbs.
		 */
		val ORB_SPLIT_SIZES = intArrayOf(2477, 1237, 617, 307, 149, 73, 37, 17, 7, 3, 1) //This is indexed biggest to smallest so that we can return as soon as we found the biggest value.

		/**
		 * Returns the largest size of normal XP orb that will be spawned for the specified amount of XP. Used to split XP
		 * up into multiple orbs when an amount of XP is dropped.
		 */
		fun getMaxOrbSize(amount: Int): Int {
			for (split in ORB_SPLIT_SIZES) {
				if (amount >= split) {
					return split
				}
			}
			return 1
		}

		/**
		 * Splits the specified amount of XP into an array of acceptable XP orb sizes.
		 */
		fun splitIntoOrbSizes(amount: Int): List<Int> {
			var amount = amount
			val result: MutableList<Int> = IntArrayList()
			while (amount > 0) {
				val size = getMaxOrbSize(amount)
				result.add(size)
				amount -= size
			}
			return result
		}
	}
}