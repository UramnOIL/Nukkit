package cn.nukkit.entity.mob

import cn.nukkit.entity.EntityArthropod
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author Box.
 */
class EntityEndermite(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt), EntityArthropod {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 8
	}

	override val width: Float
		get() = 0.4f

	override val height: Float
		get() = 0.3f

	override val name: String?
		get() = "Endermite"

	companion object {
		const val networkId = 55
	}
}