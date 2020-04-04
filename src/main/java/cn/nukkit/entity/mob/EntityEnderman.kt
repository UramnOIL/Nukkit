package cn.nukkit.entity.mob

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityEnderman(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt) {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 40
	}

	override val width: Float
		get() = 0.6f

	override val height: Float
		get() = 2.9f

	override val name: String?
		get() = "Enderman"

	companion object {
		const val networkId = 38
	}
}