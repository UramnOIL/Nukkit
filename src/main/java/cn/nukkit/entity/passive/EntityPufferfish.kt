package cn.nukkit.entity.passive

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Created by PetteriM1
 */
class EntityPufferfish(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {

	override val name: String?
		get() = "Pufferfish"

	override val width: Float
		get() = 0.35f

	override val height: Float
		get() = 0.35f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 3
	}

	companion object {
		const val networkId = 108
	}
}