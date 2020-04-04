package cn.nukkit.entity.mob

import cn.nukkit.entity.EntitySmite
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityZombiePigman(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt), EntitySmite {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 20
	}

	override val width: Float
		get() = 0.6f

	override val height: Float
		get() = 1.95f

	override val name: String?
		get() = "ZombiePigman"

	companion object {
		const val networkId = 36
	}
}