package cn.nukkit.entity.passive

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

class EntityCat(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {

	override val width: Float
		get() = if (this.isBaby) {
			0.3f
		} else 0.6f

	override val height: Float
		get() = if (this.isBaby) {
			0.35f
		} else 0.7f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 10
	}

	companion object {
		const val networkId = 75
	}
}