package cn.nukkit.entity.passive

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityLlama(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {

	override val width: Float
		get() = if (this.isBaby) {
			0.45f
		} else 0.9f

	override val height: Float
		get() = if (this.isBaby) {
			0.935f
		} else 1.87f

	override val eyeHeight: Float
		get() = if (this.isBaby) {
			0.65f
		} else 1.2f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 15
	}

	companion object {
		const val networkId = 29
	}
}