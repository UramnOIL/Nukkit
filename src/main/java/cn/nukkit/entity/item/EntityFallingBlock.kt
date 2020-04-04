package cn.nukkit.entity.item

import cn.nukkit.block.Block.Companion.get
import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.IntEntityData
import cn.nukkit.event.entity.EntityBlockChangeEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause
import cn.nukkit.item.Item
import cn.nukkit.level.GameRule
import cn.nukkit.level.GlobalBlockPalette
import cn.nukkit.level.Sound
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author MagicDroidX
 */
class EntityFallingBlock(chunk: FullChunk?, nbt: CompoundTag?) : Entity(chunk, nbt) {
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

	var block = 0
		protected set
	var damage = 0
		protected set

	override fun initEntity() {
		super.initEntity()
		if (namedTag != null) {
			if (namedTag!!.contains("TileID")) {
				block = namedTag!!.getInt("TileID")
			} else if (namedTag!!.contains("Tile")) {
				block = namedTag!!.getInt("Tile")
				namedTag!!.putInt("TileID", block)
			}
			if (namedTag!!.contains("Data")) {
				damage = namedTag!!.getByte("Data")
			}
		}
		if (block == 0) {
			close()
			return
		}
		setDataProperty(IntEntityData(Entity.Companion.DATA_VARIANT, GlobalBlockPalette.getOrCreateRuntimeId(block, damage)))
	}

	override fun canCollideWith(entity: Entity): Boolean {
		return false
	}

	override fun attack(source: EntityDamageEvent): Boolean {
		return source.cause == DamageCause.VOID && super.attack(source)
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
		lastUpdate = currentTick
		var hasUpdate = entityBaseTick(tickDiff)
		if (isAlive) {
			motionY -= gravity.toDouble()
			move(motionX, motionY, motionZ)
			val friction = 1 - drag
			motionX *= friction.toDouble()
			motionY *= 1 - drag.toDouble()
			motionZ *= friction.toDouble()
			val pos = Vector3(x - 0.5, y, z - 0.5).round()
			if (onGround) {
				close()
				val block = level.getBlock(pos)
				if (block.id > 0 && block.isTransparent && !block.canBeReplaced()) {
					if (level.getGameRules().getBoolean(GameRule.DO_ENTITY_DROPS)) {
						getLevel().dropItem(this, Item.get(this.block, damage, 1))
					}
				} else {
					val event = EntityBlockChangeEvent(this, block, get(block, damage))
					server!!.pluginManager.callEvent(event)
					if (!event.isCancelled) {
						getLevel().setBlock(pos, event.to, true)
						if (event.to.id == Item.ANVIL) {
							getLevel().addSound(pos, Sound.RANDOM_ANVIL_LAND)
						}
					}
				}
				hasUpdate = true
			}
			updateMovement()
		}
		timing!!.stopTiming()
		return hasUpdate || !onGround || Math.abs(motionX) > 0.00001 || Math.abs(motionY) > 0.00001 || Math.abs(motionZ) > 0.00001
	}

	override fun saveNBT() {
		namedTag!!.putInt("TileID", block)
		namedTag!!.putByte("Data", damage)
	}

	override fun canBeMovedByCurrents(): Boolean {
		return false
	}

	companion object {
		const val networkId = 66
	}
}