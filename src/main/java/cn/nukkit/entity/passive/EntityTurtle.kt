package cn.nukkit.entity.passive

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Created by PetteriM1
 */
class EntityTurtle(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {

	override val name: String?
		get() = "Turtle"

	override val width: Float
		get() = 1.2f

	override val height: Float
		get() = 0.4f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 30
	}

	companion object {
		const val networkId = 74
	}
}