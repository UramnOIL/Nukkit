package cn.nukkit.entity.item

import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityExplosive
import cn.nukkit.entity.data.IntEntityData
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.event.entity.EntityExplosionPrimeEvent
import cn.nukkit.level.Explosion
import cn.nukkit.level.GameRule
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.LevelSoundEventPacket

/**
 * @author MagicDroidX
 */
class EntityPrimedTNT @JvmOverloads constructor(chunk: FullChunk?, nbt: CompoundTag?, var source: Entity? = null) : Entity(chunk, nbt), EntityExplosive {
	override val width: Float
		get() = 0.98f

	override val length: Float
		get() = 0.98f

	override val height: Float
		get() = 0.98f

	protected override val gravity: Float
		protected get() = 0.04f

	protected override val drag: Float
		protected get() = 0.02f

	protected override val baseOffset: Float
		protected get() = 0.49f

	override fun canCollide(): Boolean {
		return false
	}

	protected var fuse = 0

	override fun attack(source: EntityDamageEvent): Boolean {
		return source.cause == DamageCause.VOID && super.attack(source)
	}

	override fun initEntity() {
		super.initEntity()
		fuse = if (namedTag!!.contains("Fuse")) {
			namedTag!!.getByte("Fuse")
		} else {
			80
		}
		this.setDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_IGNITED, true)
		this.setDataProperty(IntEntityData(Entity.Companion.DATA_FUSE_LENGTH, fuse))
		getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_FIZZ)
	}

	override fun canCollideWith(entity: Entity): Boolean {
		return false
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag!!.putByte("Fuse", fuse)
	}

	override fun onUpdate(currentTick: Int): Boolean {
		if (closed) {
			return false
		}
		timing!!.startTiming()
		val tickDiff = currentTick - lastUpdate
		if (tickDiff <= 0 && !justCreated) {
			return true
		}
		if (fuse % 5 == 0) {
			this.setDataProperty(IntEntityData(Entity.Companion.DATA_FUSE_LENGTH, fuse))
		}
		lastUpdate = currentTick
		val hasUpdate = entityBaseTick(tickDiff)
		if (isAlive) {
			motionY -= gravity.toDouble()
			move(motionX, motionY, motionZ)
			val friction = 1 - drag
			motionX *= friction.toDouble()
			motionY *= friction.toDouble()
			motionZ *= friction.toDouble()
			updateMovement()
			if (onGround) {
				motionY *= -0.5
				motionX *= 0.7
				motionZ *= 0.7
			}
			fuse -= tickDiff
			if (fuse <= 0) {
				if (level.getGameRules().getBoolean(GameRule.TNT_EXPLODES)) explode()
				kill()
			}
		}
		timing!!.stopTiming()
		return hasUpdate || fuse >= 0 || Math.abs(motionX) > 0.00001 || Math.abs(motionY) > 0.00001 || Math.abs(motionZ) > 0.00001
	}

	override fun explode() {
		val event = EntityExplosionPrimeEvent(this, 4)
		server!!.pluginManager.callEvent(event)
		if (event.isCancelled) {
			return
		}
		val explosion = Explosion(this, event.force, this)
		if (event.isBlockBreaking) {
			explosion.explodeA()
		}
		explosion.explodeB()
	}

	companion object {
		const val networkId = 65
	}

}