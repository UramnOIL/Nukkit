package cn.nukkit.entity.mob

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityMagmaCube(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt) {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 16
	}

	override val width: Float
		get() = 2.04f

	override val height: Float
		get() = 2.04f

	override val name: String?
		get() = "Magma Cube"

	companion object {
		const val networkId = 42
	}
}