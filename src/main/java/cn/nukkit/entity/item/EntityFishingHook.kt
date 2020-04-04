package cn.nukkit.entity.item

import cn.nukkit.Player
import cn.nukkit.Server.Companion.broadcastPacket
import cn.nukkit.block.Block
import cn.nukkit.entity.Entity
import cn.nukkit.entity.projectile.EntityProjectile
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.entity.ProjectileHitEvent
import cn.nukkit.item.Item
import cn.nukkit.item.randomitem.Fishing
import cn.nukkit.level.MovingObjectPosition
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.particle.BubbleParticle
import cn.nukkit.level.particle.WaterParticle
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.protocol.AddEntityPacket
import cn.nukkit.network.protocol.EntityEventPacket
import java.util.*

/**
 * Created by PetteriM1
 */
class EntityFishingHook @JvmOverloads constructor(chunk: FullChunk?, nbt: CompoundTag?, shootingEntity: Entity? = null) : EntityProjectile(chunk, nbt, shootingEntity) {
	var chance = false
	var waitChance = WAIT_CHANCE * 2
	var attracted = false
	var attractTimer = 0
	var caught = false
	var coughtTimer = 0
	var fish: Vector3? = null
	var rod: Item? = null

	override val width: Float
		get() = 0.2f

	override val length: Float
		get() = 0.2f

	override val height: Float
		get() = 0.2f

	override val gravity: Float
		get() = 0.07f

	override val drag: Float
		get() = 0.05f

	override fun onUpdate(currentTick: Int): Boolean {
		var hasUpdate = super.onUpdate(currentTick)
		if (hasUpdate) {
			return false
		}
		if (this.isInsideOfWater) {
			motionX = 0.0
			motionY -= gravity * -0.04
			motionZ = 0.0
			hasUpdate = true
		} else if (isCollided && keepMovement) {
			motionX = 0.0
			motionY = 0.0
			motionZ = 0.0
			keepMovement = false
			hasUpdate = true
		}
		val random = Random()
		if (this.isInsideOfWater) {
			if (!attracted) {
				if (waitChance > 0) {
					--waitChance
				}
				if (waitChance == 0) {
					if (random.nextInt(100) < 90) {
						attractTimer = random.nextInt(40) + 20
						spawnFish()
						caught = false
						attracted = true
					} else {
						waitChance = WAIT_CHANCE
					}
				}
			} else if (!caught) {
				if (attractFish()) {
					coughtTimer = random.nextInt(20) + 30
					fishBites()
					caught = true
				}
			} else {
				if (coughtTimer > 0) {
					--coughtTimer
				}
				if (coughtTimer == 0) {
					attracted = false
					caught = false
					waitChance = WAIT_CHANCE * 3
				}
			}
		}
		return hasUpdate
	}

	val waterHeight: Int
		get() {
			for (y in this.floorY..255) {
				val id = level.getBlockIdAt(this.floorX, y, this.floorZ)
				if (id == Block.AIR) {
					return y
				}
			}
			return this.floorY
		}

	fun fishBites() {
		val pk = EntityEventPacket()
		pk.eid = getId()
		pk.event = EntityEventPacket.FISH_HOOK_HOOK
		broadcastPacket(level.players.values, pk)
		val bubblePk = EntityEventPacket()
		bubblePk.eid = getId()
		bubblePk.event = EntityEventPacket.FISH_HOOK_BUBBLE
		broadcastPacket(level.players.values, bubblePk)
		val teasePk = EntityEventPacket()
		teasePk.eid = getId()
		teasePk.event = EntityEventPacket.FISH_HOOK_TEASE
		broadcastPacket(level.players.values, teasePk)
		val random = Random()
		for (i in 0..4) {
			level.addParticle(BubbleParticle(setComponents(
					x + random.nextDouble() * 0.5 - 0.25,
					waterHeight.toDouble(),
					z + random.nextDouble() * 0.5 - 0.25
			)))
		}
	}

	fun spawnFish() {
		val random = Random()
		fish = Vector3(
				x + (random.nextDouble() * 1.2 + 1) * if (random.nextBoolean()) -1 else 1,
				waterHeight.toDouble(),
				z + (random.nextDouble() * 1.2 + 1) * if (random.nextBoolean()) -1 else 1
		)
	}

	fun attractFish(): Boolean {
		val multiply = 0.1
		fish!!.setComponents(
				fish!!.x + (x - fish!!.x) * multiply,
				fish!!.y,
				fish!!.z + (z - fish!!.z) * multiply
		)
		if (Random().nextInt(100) < 85) {
			level.addParticle(WaterParticle(fish))
		}
		val dist = Math.abs(Math.sqrt(x * x + z * z) - Math.sqrt(fish!!.x * fish!!.x + fish!!.z * fish!!.z))
		return if (dist < 0.15) {
			true
		} else false
	}

	fun reelLine() {
		if (shootingEntity is Player && caught) {
			val item = Fishing.getFishingResult(rod)
			val experience = Random().nextInt(3 - 1 + 1) + 1
			val motion: Vector3
			if (shootingEntity != null) {
				motion = shootingEntity.subtract(this).multiply(0.1)
				motion.y += Math.sqrt(shootingEntity.distance(this)) * 0.08
			} else {
				motion = Vector3()
			}
			val itemTag = NBTIO.putItemHelper(item)
			itemTag.name = "Item"
			val itemEntity = EntityItem(
					level.getChunk(x.toInt() shr 4, z.toInt() shr 4, true),
					CompoundTag()
							.putList(ListTag<DoubleTag>("Pos")
									.add(DoubleTag("", getX()))
									.add(DoubleTag("", waterHeight.toDouble()))
									.add(DoubleTag("", getZ())))
							.putList(ListTag<DoubleTag>("Motion")
									.add(DoubleTag("", motion.x))
									.add(DoubleTag("", motion.y))
									.add(DoubleTag("", motion.z)))
							.putList(ListTag<FloatTag>("Rotation")
									.add(FloatTag("", Random().nextFloat() * 360))
									.add(FloatTag("", 0)))
							.putShort("Health", 5).putCompound("Item", itemTag).putShort("PickupDelay", 1))
			if (shootingEntity != null && shootingEntity is Player) {
				itemEntity.setOwner(shootingEntity.name)
			}
			itemEntity.spawnToAll()
			val player = shootingEntity as Player
			if (experience > 0) {
				player.addExperience(experience)
			}
		}
		if (shootingEntity is Player) {
			val pk = EntityEventPacket()
			pk.eid = getId()
			pk.event = EntityEventPacket.FISH_HOOK_TEASE
			broadcastPacket(level.players.values, pk)
		}
		if (!closed) {
			kill()
			close()
		}
	}

	override fun spawnTo(player: Player) {
		val pk = AddEntityPacket()
		pk.entityRuntimeId = getId()
		pk.entityUniqueId = getId()
		pk.type = networkId
		pk.x = x.toFloat()
		pk.y = y.toFloat()
		pk.z = z.toFloat()
		pk.speedX = motionX.toFloat()
		pk.speedY = motionY.toFloat()
		pk.speedZ = motionZ.toFloat()
		pk.yaw = yaw.toFloat()
		pk.pitch = pitch.toFloat()
		var ownerId: Long = -1
		if (shootingEntity != null) {
			ownerId = shootingEntity.id
		}
		pk.metadata = dataProperties!!.putLong(Entity.Companion.DATA_OWNER_EID, ownerId)
		player.dataPacket(pk)
		super.spawnTo(player)
	}

	override fun onCollideWithEntity(entity: Entity) {
		server!!.pluginManager.callEvent(ProjectileHitEvent(this, MovingObjectPosition.fromEntity(entity)))
		val damage = this.resultDamage.toFloat()
		val ev: EntityDamageEvent
		ev = if (shootingEntity == null) {
			EntityDamageByEntityEvent(this, entity, DamageCause.PROJECTILE, damage)
		} else {
			EntityDamageByChildEntityEvent(shootingEntity, this, entity, DamageCause.PROJECTILE, damage)
		}
		entity.attack(ev)
	}

	companion object {
		const val networkId = 77
		const val WAIT_CHANCE = 120
		const val CHANCE = 40
	}
}