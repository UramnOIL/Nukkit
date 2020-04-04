package cn.nukkit.entity.mob

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityShulker(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt) {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 30
	}

	override val width: Float
		get() = 1f

	override val height: Float
		get() = 1f

	override val name: String?
		get() = "Shulker"

	companion object {
		const val networkId = 54
	}
}