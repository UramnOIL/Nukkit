package cn.nukkit.entity.mob

import cn.nukkit.entity.EntitySmite
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityWitherSkeleton(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt), EntitySmite {

	override fun initEntity() {
		super.initEntity()
	}

	override val width: Float
		get() = 0.7f

	override val height: Float
		get() = 2.4f

	override val name: String?
		get() = "WitherSkeleton"

	companion object {
		const val networkId = 48
	}
}