package cn.nukkit.entity.mob

import cn.nukkit.entity.EntitySmite
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityWither(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt), EntitySmite {

	override val width: Float
		get() = 0.9f

	override val height: Float
		get() = 3.5f

	override fun initEntity() {
		super.initEntity()
		maxHealth = 300
	}

	override val name: String?
		get() = "Wither"

	companion object {
		const val networkId = 52
	}
}