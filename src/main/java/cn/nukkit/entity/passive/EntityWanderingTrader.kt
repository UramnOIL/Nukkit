package cn.nukkit.entity.passive

import cn.nukkit.entity.EntityCreature
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

class EntityWanderingTrader(chunk: FullChunk?, nbt: CompoundTag?) : EntityCreature(chunk, nbt), EntityNPC {
	override val width: Float
		get() = 0.6f

	override val height: Float
		get() = 1.8f

	override val name: String?
		get() = "Wandering Trader"

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 20
	}

	companion object {
		const val networkId = 118
	}
}