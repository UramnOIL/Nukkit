package cn.nukkit.entity.mob

import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityEnderDragon(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt) {

	override val width: Float
		get() = 13f

	override val height: Float
		get() = 4f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 200
	}

	override val name: String?
		get() = "EnderDragon"

	companion object {
		const val networkId = 53
	}
}