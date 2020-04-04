package cn.nukkit.entity.mob

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityVex(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt) {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 14
	}

	override val width: Float
		get() = 0.4f

	override val height: Float
		get() = 0.8f

	override val name: String?
		get() = "Vex"

	companion object {
		const val networkId = 105
	}
}