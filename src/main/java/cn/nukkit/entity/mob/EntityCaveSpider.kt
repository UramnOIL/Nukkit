package cn.nukkit.entity.mob

import cn.nukkit.entity.EntityArthropod
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityCaveSpider(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt), EntityArthropod {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 12
	}

	override val width: Float
		get() = 0.7f

	override val height: Float
		get() = 0.5f

	override val name: String?
		get() = "CaveSpider"

	companion object {
		const val networkId = 40
	}
}