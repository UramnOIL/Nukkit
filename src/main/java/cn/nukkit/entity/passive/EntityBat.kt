package cn.nukkit.entity.passive

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityBat(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {

	override val width: Float
		get() = 0.5f

	override val height: Float
		get() = 0.9f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 6
	}

	companion object {
		const val networkId = 19
	}
}