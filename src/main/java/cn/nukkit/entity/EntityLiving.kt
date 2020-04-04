package cn.nukkit.entity

import cn.nukkit.Player
import cn.nukkit.Server.Companion.broadcastPacket
import cn.nukkit.block.Block
import cn.nukkit.block.BlockMagma
import cn.nukkit.entity.data.ShortEntityData
import cn.nukkit.entity.passive.EntityWaterAnimal
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.entity.EntityDeathEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTurtleShell
import cn.nukkit.level.GameRule
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.network.protocol.AnimatePacket
import cn.nukkit.network.protocol.EntityEventPacket
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.potion.Effect
import cn.nukkit.utils.BlockIterator
import co.aikar.timings.Timings
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class EntityLiving(chunk: FullChunk?, nbt: CompoundTag?) : Entity(chunk, nbt), EntityDamageable {
	protected override val gravity: Float
		protected get() = 0.08f

	protected override val drag: Float
		protected get() = 0.02f

	protected var attackTime = 0
	protected var invisible = false
	open var movementSpeed = 0.1f
	protected var turtleTicks = 200
	override fun initEntity() {
		super.initEntity()
		if (namedTag!!.contains("HealF")) {
			namedTag!!.putFloat("Health", namedTag!!.getShort("HealF").toFloat())
			namedTag!!.remove("HealF")
		}
		if (!namedTag!!.contains("Health") || namedTag!!["Health"] !is FloatTag) {
			namedTag!!.putFloat("Health", maxHealth.toFloat())
		}
		health = namedTag!!.getFloat("Health")
	}

	override fun setHealth(health: Float) {
		val wasAlive = this.isAlive
		super.setHealth(health)
		if (this.isAlive && !wasAlive) {
			val pk = EntityEventPacket()
			pk.eid = getId()
			pk.event = EntityEventPacket.RESPAWN
			broadcastPacket(hasSpawned.values, pk)
		}
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag!!.putFloat("Health", getHealth())
	}

	fun hasLineOfSight(entity: Entity?): Boolean {
		//todo
		return true
	}

	fun collidingWith(ent: Entity) { // can override (IronGolem|Bats)
		ent.applyEntityCollision(this)
	}

	override fun attack(source: EntityDamageEvent): Boolean {
		if (noDamageTicks > 0) {
			return false
		} else if (attackTime > 0) {
			val lastCause = getLastDamageCause()
			if (lastCause != null && lastCause.damage >= source.damage) {
				return false
			}
		}
		return if (super.attack(source)) {
			if (source is EntityDamageByEntityEvent) {
				var damager = source.damager
				if (source is EntityDamageByChildEntityEvent) {
					damager = source.child
				}

				//Critical hit
				if (damager is Player && !damager.onGround) {
					val animate = AnimatePacket()
					animate.action = AnimatePacket.Action.CRITICAL_HIT
					animate.eid = getId()
					getLevel().addChunkPacket(damager.getChunkX(), damager.getChunkZ(), animate)
					getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_ATTACK_STRONG)
					source.setDamage(source.getDamage() * 1.5f)
				}
				if (damager.isOnFire && damager !is Player) {
					setOnFire(2 * server!!.getDifficulty())
				}
				val deltaX = x - damager.x
				val deltaZ = z - damager.z
				knockBack(damager, source.getDamage().toDouble(), deltaX, deltaZ, source.knockBack.toDouble())
			}
			val pk = EntityEventPacket()
			pk.eid = getId()
			pk.event = if (getHealth() <= 0) EntityEventPacket.DEATH_ANIMATION else EntityEventPacket.HURT_ANIMATION
			broadcastPacket(hasSpawned.values, pk)
			attackTime = source.attackCooldown
			true
		} else {
			false
		}
	}

	@JvmOverloads
	fun knockBack(attacker: Entity?, damage: Double, x: Double, z: Double, base: Double = 0.4) {
		var f = Math.sqrt(x * x + z * z)
		if (f <= 0) {
			return
		}
		f = 1 / f
		var motion = Vector3(motionX, motionY, motionZ)
		motion.x /= 2.0
		motion.y /= 2.0
		motion.z /= 2.0
		motion.x += x * f * base
		motion.y += base
		motion.z += z * f * base
		if (motion.y > base) {
			motion.y = base
		}
		motion = motion
	}

	override fun kill() {
		if (!this.isAlive) {
			return
		}
		super.kill()
		val ev = EntityDeathEvent(this, drops)
		server!!.pluginManager.callEvent(ev)
		if (level.getGameRules().getBoolean(GameRule.DO_ENTITY_DROPS)) {
			for (item in ev.drops) {
				getLevel().dropItem(this, item)
			}
		}
	}

	override fun entityBaseTick(): Boolean {
		return this.entityBaseTick(1)
	}

	override fun entityBaseTick(tickDiff: Int): Boolean {
		Timings.livingEntityBaseTickTimer.startTiming()
		var isBreathing = !this.isInsideOfWater
		if (this is Player && (this.isCreative || this.isSpectator)) {
			isBreathing = true
		}
		if (this is Player) {
			if (!isBreathing && this.inventory.helmet is ItemTurtleShell) {
				if (turtleTicks > 0) {
					isBreathing = true
					turtleTicks--
				}
			} else {
				turtleTicks = 200
			}
		}
		this.setDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_BREATHING, isBreathing)
		var hasUpdate = super.entityBaseTick(tickDiff)
		if (this.isAlive) {
			if (this.isInsideOfSolid) {
				hasUpdate = true
				this.attack(EntityDamageEvent(this, DamageCause.SUFFOCATION, 1))
			}
			if (this.isOnLadder || hasEffect(Effect.LEVITATION)) {
				resetFallDistance()
			}
			if (!hasEffect(Effect.WATER_BREATHING) && this.isInsideOfWater) {
				if (this is EntityWaterAnimal || this is Player && (this.isCreative || this.isSpectator)) {
					airTicks = 400
				} else {
					if (turtleTicks == 0 || turtleTicks == 200) {
						hasUpdate = true
						var airTicks = airTicks - tickDiff
						if (airTicks <= -20) {
							airTicks = 0
							this.attack(EntityDamageEvent(this, DamageCause.DROWNING, 2))
						}
						airTicks = airTicks
					}
				}
			} else {
				if (this is EntityWaterAnimal) {
					hasUpdate = true
					var airTicks = airTicks - tickDiff
					if (airTicks <= -20) {
						airTicks = 0
						this.attack(EntityDamageEvent(this, DamageCause.SUFFOCATION, 2))
					}
					airTicks = airTicks
				} else {
					var airTicks = airTicks
					if (airTicks < 400) {
						airTicks = Math.min(400, airTicks + tickDiff * 5)
					}
				}
			}
		}
		if (attackTime > 0) {
			attackTime -= tickDiff
		}
		if (riding == null) {
			for (entity in level.getNearbyEntities(boundingBox!!.grow(0.20000000298023224, 0.0, 0.20000000298023224), this)) {
				if (entity is EntityRideable) {
					collidingWith(entity)
				}
			}
		}

		// Used to check collisions with magma blocks
		val block = level.getBlock(x.toInt(), y.toInt() - 1, z.toInt())
		(block as? BlockMagma)?.onEntityCollide(this)
		Timings.livingEntityBaseTickTimer.stopTiming()
		return hasUpdate
	}

	open val drops: Array<Item?>
		get() = arrayOfNulls(0)

	fun getLineOfSight(maxDistance: Int): Array<Block> {
		return this.getLineOfSight(maxDistance, 0)
	}

	fun getLineOfSight(maxDistance: Int, maxLength: Int): Array<Block> {
		return this.getLineOfSight(maxDistance, maxLength, arrayOf())
	}

	@Deprecated("")
	fun getLineOfSight(maxDistance: Int, maxLength: Int, transparent: Map<Int?, Any?>): Array<Block> {
		return this.getLineOfSight(maxDistance, maxLength, transparent.keys.toTypedArray())
	}

	fun getLineOfSight(maxDistance: Int, maxLength: Int, transparent: Array<Int?>?): Array<Block> {
		var maxDistance = maxDistance
		var transparent = transparent
		if (maxDistance > 120) {
			maxDistance = 120
		}
		if (transparent != null && transparent.size == 0) {
			transparent = null
		}
		val blocks: MutableList<Block> = ArrayList()
		val itr = BlockIterator(level, this.position, this.directionVector, this.eyeHeight, maxDistance)
		while (itr.hasNext()) {
			val block = itr.next()
			blocks.add(block)
			if (maxLength != 0 && blocks.size > maxLength) {
				blocks.removeAt(0)
			}
			val id = block.id
			if (transparent == null) {
				if (id != 0) {
					break
				}
			} else {
				if (Arrays.binarySearch(transparent, id) < 0) {
					break
				}
			}
		}
		return blocks.toTypedArray()
	}

	fun getTargetBlock(maxDistance: Int): Block? {
		return getTargetBlock(maxDistance, arrayOf())
	}

	@Deprecated("")
	fun getTargetBlock(maxDistance: Int, transparent: Map<Int?, Any?>): Block? {
		return getTargetBlock(maxDistance, transparent.keys.toTypedArray())
	}

	fun getTargetBlock(maxDistance: Int, transparent: Array<Int?>?): Block? {
		try {
			val blocks = this.getLineOfSight(maxDistance, 1, transparent)
			val block = blocks[0]
			if (block != null) {
				if (transparent != null && transparent.size != 0) {
					if (Arrays.binarySearch(transparent, block.id) < 0) {
						return block
					}
				} else {
					return block
				}
			}
		} catch (ignored: Exception) {
		}
		return null
	}

	var airTicks: Int
		get() = getDataPropertyShort(Entity.Companion.DATA_AIR)
		set(ticks) {
			this.setDataProperty(ShortEntityData(Entity.Companion.DATA_AIR, ticks))
		}

}