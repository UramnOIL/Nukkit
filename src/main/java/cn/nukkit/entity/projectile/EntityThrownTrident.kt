package cn.nukkit.entity.projectile

import cn.nukkit.Player
import cn.nukkit.entity.Entity
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.entity.ProjectileHitEvent
import cn.nukkit.item.Item
import cn.nukkit.level.MovingObjectPosition
import cn.nukkit.level.Position
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.network.protocol.AddEntityPacket
import cn.nukkit.network.protocol.LevelSoundEventPacket
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by PetteriM1
 */
class EntityThrownTrident @JvmOverloads constructor(chunk: FullChunk?, nbt: CompoundTag?, shootingEntity: Entity? = null, critical: Boolean = false) : EntityProjectile(chunk, nbt, shootingEntity) {
	protected var trident: Item? = null

	override val width: Float
		get() = 0.05f

	override val length: Float
		get() = 0.5f

	override val height: Float
		get() = 0.05f

	public override fun getGravity(): Float {
		return 0.04f
	}

	public override fun getDrag(): Float {
		return 0.01f
	}

	protected override var gravity = 0.04f
	protected override var drag = 0.01f
	override fun initEntity() {
		super.initEntity()
		damage = if (namedTag!!.contains("damage")) namedTag!!.getDouble("damage") else 8
		trident = if (namedTag!!.contains("Trident")) NBTIO.getItemHelper(namedTag!!.getCompound("Trident")) else Item.get(0)
		closeOnCollide = false
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag!!.put("Trident", NBTIO.putItemHelper(trident))
	}

	var item: Item?
		get() = if (trident != null) trident!!.clone() else Item.get(0)
		set(item) {
			trident = item!!.clone()
		}

	fun setCritical() {
		this.critical = true
	}

	var isCritical: Boolean
		get() = getDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_CRITICAL)
		set(value) {
			this.setDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_CRITICAL, value)
		}

	override val resultDamage: Int
		get() {
			var base = super.getResultDamage()
			if (isCritical) {
				base += ThreadLocalRandom.current().nextInt(base / 2 + 2)
			}
			return base
		}

	protected override val baseDamage: Double
		protected get() = 8

	override fun onUpdate(currentTick: Int): Boolean {
		if (closed) {
			return false
		}
		timing!!.startTiming()
		if (isCollided && !hadCollision) {
			getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_ITEM_TRIDENT_HIT_GROUND)
		}
		var hasUpdate = super.onUpdate(currentTick)
		if (onGround || hadCollision) {
			this.critical = false
		}
		if (age > 1200) {
			close()
			hasUpdate = true
		}
		timing!!.stopTiming()
		return hasUpdate
	}

	override fun spawnTo(player: Player) {
		val pk = AddEntityPacket()
		pk.type = networkId
		pk.entityUniqueId = getId()
		pk.entityRuntimeId = getId()
		pk.x = x.toFloat()
		pk.y = y.toFloat()
		pk.z = z.toFloat()
		pk.speedX = motionX.toFloat()
		pk.speedY = motionY.toFloat()
		pk.speedZ = motionZ.toFloat()
		pk.yaw = yaw.toFloat()
		pk.pitch = pitch.toFloat()
		pk.metadata = dataProperties
		player.dataPacket(pk)
		super.spawnTo(player)
	}

	override fun onCollideWithEntity(entity: Entity) {
		server!!.pluginManager.callEvent(ProjectileHitEvent(this, MovingObjectPosition.fromEntity(entity)))
		val damage = resultDamage.toFloat()
		val ev: EntityDamageEvent
		ev = if (shootingEntity == null) {
			EntityDamageByEntityEvent(this, entity, DamageCause.PROJECTILE, damage)
		} else {
			EntityDamageByChildEntityEvent(shootingEntity, this, entity, DamageCause.PROJECTILE, damage)
		}
		entity.attack(ev)
		getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_ITEM_TRIDENT_HIT)
		hadCollision = true
		close()
		val newTrident = create("ThrownTrident", this)
		(newTrident as EntityThrownTrident?)!!.item = trident
		newTrident!!.spawnToAll()
	}

	fun create(type: Any, source: Position, vararg args: Any?): Entity? {
		val chunk = source.getLevel().getChunk(source.x.toInt() shr 4, source.z.toInt() shr 4) ?: return null
		val nbt = CompoundTag()
				.putList(ListTag<DoubleTag>("Pos")
						.add(DoubleTag("", source.x + 0.5))
						.add(DoubleTag("", source.y))
						.add(DoubleTag("", source.z + 0.5)))
				.putList(ListTag<DoubleTag>("Motion")
						.add(DoubleTag("", 0))
						.add(DoubleTag("", 0))
						.add(DoubleTag("", 0)))
				.putList(ListTag<FloatTag>("Rotation")
						.add(FloatTag("", Random().nextFloat() * 360))
						.add(FloatTag("", 0)))
		return Entity.Companion.createEntity(type.toString(), chunk, nbt, *args)
	}

	companion object {
		const val networkId = 73
		const val DATA_SOURCE_ID = 17
	}
}