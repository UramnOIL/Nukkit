package cn.nukkit.entity.passive

import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityAgeable
import cn.nukkit.entity.EntityCreature
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class EntityWaterAnimal(chunk: FullChunk?, nbt: CompoundTag?) : EntityCreature(chunk, nbt), EntityAgeable {
	override val isBaby: Boolean
		get() = getDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_BABY)
}