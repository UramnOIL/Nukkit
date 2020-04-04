package cn.nukkit.entity

import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.BlockFace
import cn.nukkit.nbt.tag.CompoundTag

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class EntityHanging(chunk: FullChunk?, nbt: CompoundTag?) : Entity(chunk, nbt) {
	protected override var direction = 0
	override fun initEntity() {
		super.initEntity()
		maxHealth = 1
		setHealth(1f)
		if (namedTag!!.contains("Direction")) {
			direction = namedTag!!.getByte("Direction")
		} else if (namedTag!!.contains("Dir")) {
			val d = namedTag!!.getByte("Dir")
			if (d == 2) {
				direction = 0
			} else if (d == 0) {
				direction = 2
			}
		}
	}

	override fun saveNBT() {
		super.saveNBT()
		namedTag!!.putByte("Direction", getDirection()!!.horizontalIndex)
		namedTag!!.putInt("TileX", x.toInt())
		namedTag!!.putInt("TileY", y.toInt())
		namedTag!!.putInt("TileZ", z.toInt())
	}

	override fun getDirection(): BlockFace? {
		return BlockFace.fromIndex(direction)
	}

	override fun onUpdate(currentTick: Int): Boolean {
		if (closed) {
			return false
		}
		if (!this.isAlive) {
			despawnFromAll()
			if (!isPlayer) {
				close()
			}
			return true
		}
		if (lastYaw != yaw || lastX != x || lastY != y || lastZ != z) {
			despawnFromAll()
			direction = (yaw / 90).toInt()
			lastYaw = yaw
			lastX = x
			lastY = y
			lastZ = z
			spawnToAll()
			return true
		}
		return false
	}

	protected val isSurfaceValid: Boolean
		protected get() = true
}