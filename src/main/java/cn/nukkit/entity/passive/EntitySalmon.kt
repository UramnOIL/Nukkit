package cn.nukkit.entity.passive

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Created by PetteriM1
 */
class EntitySalmon(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {

	override val name: String?
		get() = "Salmon"

	override val width: Float
		get() = 0.7f

	override val height: Float
		get() = 0.4f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 3
	}

	companion object {
		const val networkId = 109
	}
}