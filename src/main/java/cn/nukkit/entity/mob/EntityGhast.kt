package cn.nukkit.entity.mob

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityGhast(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt) {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 10
	}

	override val width: Float
		get() = 4

	override val height: Float
		get() = 4

	override val name: String?
		get() = "Ghast"

	companion object {
		const val networkId = 41
	}
}