package cn.nukkit.entity.mob

import cn.nukkit.entity.EntityArthropod
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntitySilverfish(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt), EntityArthropod {

	override val name: String?
		get() = "Silverfish"

	override val width: Float
		get() = 0.4f

	override val height: Float
		get() = 0.3f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 8
	}

	companion object {
		const val networkId = 39
	}
}