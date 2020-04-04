package cn.nukkit.entity.passive

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

class EntityPanda(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {

	override val length: Float
		get() = 1.825f

	override val width: Float
		get() = 1.125f

	override val height: Float
		get() = 1.25f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 20
	}

	companion object {
		const val networkId = 113
	}
}