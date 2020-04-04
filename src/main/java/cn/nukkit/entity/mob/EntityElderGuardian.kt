package cn.nukkit.entity.mob

import cn.nukkit.entity.Entity
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityElderGuardian(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt) {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 80
		this.setDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_ELDER, true)
	}

	override val width: Float
		get() = 1.9975f

	override val height: Float
		get() = 1.9975f

	override val name: String?
		get() = "Elder Guardian"

	companion object {
		const val networkId = 50
	}
}