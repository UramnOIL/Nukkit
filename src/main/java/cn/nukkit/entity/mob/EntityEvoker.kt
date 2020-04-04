package cn.nukkit.entity.mob

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityEvoker(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt) {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 24
	}

	override val width: Float
		get() = 0.6f

	override val height: Float
		get() = 1.95f

	override val name: String?
		get() = "Evoker"

	companion object {
		const val networkId = 104
	}
}