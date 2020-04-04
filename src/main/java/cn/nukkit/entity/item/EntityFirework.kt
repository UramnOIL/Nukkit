package cn.nukkit.entity.item

import cn.nukkit.Server.Companion.broadcastPacket
import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.ByteEntityData
import cn.nukkit.entity.data.IntEntityData
import cn.nukkit.entity.data.NBTEntityData
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.item.Item
import cn.nukkit.item.ItemFirework
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.EntityEventPacket
import cn.nukkit.network.protocol.LevelSoundEventPacket
import java.util.*

/**
 * @author CreeperFace
 */
class EntityFirework(chunk: FullChunk?, nbt: CompoundTag) : Entity(chunk, nbt) {
	private var fireworkAge = 0
	private val lifetime: Int
	private var firework: Item? = null

	override fun onUpdate(currentTick: Int): Boolean {
		if (closed) {
			return false
		}
		val tickDiff = currentTick - lastUpdate
		if (tickDiff <= 0 && !justCreated) {
			return true
		}
		lastUpdate = currentTick
		timing!!.startTiming()
		var hasUpdate = this.entityBaseTick(tickDiff)
		if (this.isAlive) {
			motionX *= 1.15
			motionZ *= 1.15
			motionY += 0.04
			move(motionX, motionY, motionZ)
			updateMovement()
			val f = Math.sqrt(motionX * motionX + motionZ * motionZ).toFloat()
			yaw = (Math.atan2(motionX, motionZ) * (180.0 / Math.PI)) as Float.toDouble()
			pitch = (Math.atan2(motionY, f.toDouble()) * (180.0 / Math.PI)) as Float.toDouble()
			if (fireworkAge == 0) {
				getLevel().addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_LAUNCH)
			}
			fireworkAge++
			hasUpdate = true
			if (fireworkAge >= lifetime) {
				val pk = EntityEventPacket()
				pk.data = 0
				pk.event = EntityEventPacket.FIREWORK_EXPLOSION
				pk.eid = getId()
				level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_LARGE_BLAST, -1, networkId)
				broadcastPacket(viewers.values, pk)
				kill()
				hasUpdate = true
			}
		}
		timing!!.stopTiming()
		return hasUpdate || !onGround || Math.abs(motionX) > 0.00001 || Math.abs(motionY) > 0.00001 || Math.abs(motionZ) > 0.00001
	}

	override fun attack(source: EntityDamageEvent): Boolean {
		return ((source.cause == DamageCause.VOID || source.cause == DamageCause.FIRE_TICK || source.cause == DamageCause.ENTITY_EXPLOSION || source.cause == DamageCause.BLOCK_EXPLOSION)
				&& super.attack(source))
	}

	fun setFirework(item: Item) {
		firework = item
		this.setDataProperty(NBTEntityData(Entity.Companion.DATA_DISPLAY_ITEM, item.namedTag))
	}

	override val width: Float
		get() = 0.25f

	override val height: Float
		get() = 0.25f

	companion object {
		const val networkId = 72
	}

	init {
		val rand = Random()
		lifetime = 30 + rand.nextInt(6) + rand.nextInt(7)
		motionX = rand.nextGaussian() * 0.001
		motionZ = rand.nextGaussian() * 0.001
		motionY = 0.05
		firework = if (nbt.contains("FireworkItem")) {
			NBTIO.getItemHelper(nbt.getCompound("FireworkItem"))
		} else {
			ItemFirework()
		}
		this.setDataProperty(NBTEntityData(Entity.Companion.DATA_DISPLAY_ITEM, firework!!.namedTag))
		this.setDataProperty(IntEntityData(Entity.Companion.DATA_DISPLAY_OFFSET, 1))
		this.setDataProperty(ByteEntityData(Entity.Companion.DATA_HAS_DISPLAY, 1))
	}
}