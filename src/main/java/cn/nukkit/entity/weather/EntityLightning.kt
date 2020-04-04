package cn.nukkit.entity.weather

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockFire
import cn.nukkit.block.BlockID
import cn.nukkit.entity.Entity
import cn.nukkit.event.block.BlockIgniteEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.level.GameRule
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.LevelSoundEventPacket
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by boybook on 2016/2/27.
 */
class EntityLightning(chunk: FullChunk?, nbt: CompoundTag?) : Entity(chunk, nbt), EntityLightningStrike {
	override var isEffect = true
	var state = 0
	var liveTime = 0

	override fun initEntity() {
		super.initEntity()
		setHealth(4f)
		maxHealth = 4
		state = 2
		liveTime = ThreadLocalRandom.current().nextInt(3) + 1
		if (isEffect && level.gameRules.getBoolean(GameRule.DO_FIRE_TICK) && server!!.getDifficulty() >= 2) {
			val block = this.levelBlock
			if (block.id == 0 || block.id == Block.TALL_GRASS) {
				val fire = get(BlockID.FIRE) as BlockFire
				fire.x = block.x
				fire.y = block.y
				fire.z = block.z
				fire.level = level
				getLevel().setBlock(fire, fire, true)
				if (fire.isBlockTopFacingSurfaceSolid(fire.down()) || fire.canNeighborBurn()) {
					val e = BlockIgniteEvent(block, null, this, BlockIgniteEvent.BlockIgniteCause.LIGHTNING)
					getServer().pluginManager.callEvent(e)
					if (!e.isCancelled) {
						level.setBlock(fire, fire, true)
						level.scheduleUpdate(fire, fire.tickRate() + ThreadLocalRandom.current().nextInt(10))
					}
				}
			}
		}
	}

	override fun attack(source: EntityDamageEvent): Boolean {
		//false?
		source.damage = 0f
		return super.attack(source)
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
		this.entityBaseTick(tickDiff)
		if (state == 2) {
			level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_THUNDER)
			level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_EXPLODE)
		}
		state--
		if (state < 0) {
			if (liveTime == 0) {
				close()
				return false
			} else if (state < -ThreadLocalRandom.current().nextInt(10)) {
				liveTime--
				state = 1
				if (isEffect && level.gameRules.getBoolean(GameRule.DO_FIRE_TICK)) {
					val block = this.levelBlock
					if (block.id == Block.AIR || block.id == Block.TALL_GRASS) {
						val e = BlockIgniteEvent(block, null, this, BlockIgniteEvent.BlockIgniteCause.LIGHTNING)
						getServer().pluginManager.callEvent(e)
						if (!e.isCancelled) {
							val fire = get(BlockID.FIRE)
							level.setBlock(block, fire)
							getLevel().scheduleUpdate(fire, fire.tickRate())
						}
					}
				}
			}
		}
		if (state >= 0) {
			if (isEffect) {
				val bb = getBoundingBox().grow(3.0, 3.0, 3.0)
				bb.maxX = bb.maxX + 6
				for (entity in level.getCollidingEntities(bb, this)) {
					entity.onStruckByLightning(this)
				}
			}
		}
		return true
	}

	companion object {
		const val networkId = 93
	}
}