package cn.nukkit.entity.item

import cn.nukkit.Player
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityExplosive
import cn.nukkit.entity.data.IntEntityData
import cn.nukkit.event.entity.EntityExplosionPrimeEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemMinecartTNT
import cn.nukkit.level.Explosion
import cn.nukkit.level.GameRule
import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.utils.MinecartType
import java.util.concurrent.ThreadLocalRandom

/**
 * Author: Adam Matthew [larryTheCoder]
 *
 *
 * Nukkit Project.
 */
class EntityMinecartTNT(chunk: FullChunk?, nbt: CompoundTag?) : EntityMinecartAbstract(chunk, nbt), EntityExplosive {
	private var fuse = 0
	private val activated = false
	override val isRideable: Boolean
		get() = false

	override fun initEntity() {
		super.initEntity()
		fuse = if (namedTag!!.contains("TNTFuse")) {
			namedTag!!.getByte("TNTFuse")
		} else {
			80
		}
		this.setDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_CHARGED, false)
	}

	override fun onUpdate(currentTick: Int): Boolean {
		timing!!.startTiming()
		if (fuse < 80) {
			val tickDiff = currentTick - lastUpdate
			lastUpdate = currentTick
			if (fuse % 5 == 0) {
				setDataProperty(IntEntityData(Entity.Companion.DATA_FUSE_LENGTH, fuse))
			}
			fuse -= tickDiff
			if (isAlive && fuse <= 0) {
				if (level.getGameRules().getBoolean(GameRule.TNT_EXPLODES)) {
					this.explode(ThreadLocalRandom.current().nextInt(5).toDouble())
				}
				close()
				return false
			}
		}
		timing!!.stopTiming()
		return super.onUpdate(currentTick)
	}

	public override fun activate(x: Int, y: Int, z: Int, flag: Boolean) {
		level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_IGNITE)
		fuse = 79
	}

	override fun explode() {
		explode(0.0)
	}

	fun explode(square: Double) {
		var root = Math.sqrt(square)
		if (root > 5.0) {
			root = 5.0
		}
		val event = EntityExplosionPrimeEvent(this, 4.0 + ThreadLocalRandom.current().nextDouble() * 1.5 * root)
		server!!.pluginManager.callEvent(event)
		if (event.isCancelled) {
			return
		}
		val explosion = Explosion(this, event.force, this)
		if (event.isBlockBreaking) {
			explosion.explodeA()
		}
		explosion.explodeB()
		close()
	}

	override fun dropItem() {
		level.dropItem(this, ItemMinecartTNT())
	}

	override val type: MinecartType
		get() = MinecartType.valueOf(3)

	override fun saveNBT() {
		super.saveNBT()
		super.namedTag!!.putInt("TNTFuse", fuse)
	}

	override fun onInteract(player: Player, item: Item, clickedPos: Vector3?): Boolean {
		val interact = super.onInteract(player, item, clickedPos)
		if (item.id == Item.FLINT_AND_STEEL || item.id == Item.FIRE_CHARGE) {
			level.addLevelSoundEvent(this, LevelSoundEventPacket.SOUND_IGNITE)
			fuse = 79
			return true
		}
		return interact
	}

	override fun mountEntity(entity: Entity, mode: Byte): Boolean {
		return false
	}

	companion object {
		const val networkId = 97
	}

	init {
		super.setDisplayBlock(get(BlockID.TNT), false)
	}
}