package cn.nukkit.entity.mob

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityGuardian(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt) {

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 30
	}

	override val name: String?
		get() = "Guardian"

	override val width: Float
		get() = 0.85f

	override val height: Float
		get() = 0.85f

	companion object {
		const val networkId = 49
	}
}