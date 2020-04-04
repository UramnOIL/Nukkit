package cn.nukkit.entity.item

import cn.nukkit.Player
import cn.nukkit.entity.EntityLiving
import cn.nukkit.entity.passive.EntityWaterAnimal
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.MinecartType

/**
 * Created by Snake1999 on 2016/1/30.
 * Package cn.nukkit.entity.item in project Nukkit.
 */
class EntityMinecartEmpty(chunk: FullChunk?, nbt: CompoundTag?) : EntityMinecartAbstract(chunk, nbt) {

	override val type: MinecartType
		get() = MinecartType.valueOf(0)

	override val isRideable: Boolean
		get() = true

	override fun activate(x: Int, y: Int, z: Int, flag: Boolean) {
		if (flag) {
			if (riding != null) {
				mountEntity(riding)
			}
			// looks like MCPE and MCPC not same XD
			// removed rolling feature from here because of MCPE logic?
		}
	}

	override fun onUpdate(currentTick: Int): Boolean {
		var update = super.onUpdate(currentTick)
		if (passengers.isEmpty()) {
			for (entity in level.getCollidingEntities(boundingBox!!.grow(0.20000000298023224, 0.0, 0.20000000298023224), this)) {
				if (entity.riding != null || entity !is EntityLiving || entity is Player || entity is EntityWaterAnimal) {
					continue
				}
				this.mountEntity(entity)
				update = true
				break
			}
		}
		return update
	}

	companion object {
		const val networkId = 84
	}
}