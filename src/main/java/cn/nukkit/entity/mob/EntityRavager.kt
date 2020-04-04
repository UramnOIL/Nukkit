package cn.nukkit.entity.mob

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

class EntityRavager(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt) {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 100
	}

	override val height: Float
		get() = 1.9f

	override val width: Float
		get() = 1.2f

	override val name: String?
		get() = "Ravager"

	companion object {
		const val networkId = 59
	}
}