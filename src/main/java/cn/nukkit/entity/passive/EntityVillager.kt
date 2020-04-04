package cn.nukkit.entity.passive

import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityAgeable
import cn.nukkit.entity.EntityCreature
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

class EntityVillager(chunk: FullChunk?, nbt: CompoundTag?) : EntityCreature(chunk, nbt), EntityNPC, EntityAgeable {

	override val width: Float
		get() = if (isBaby) {
			0.3f
		} else 0.6f

	override val height: Float
		get() = if (isBaby) {
			0.9f
		} else 1.8f

	override val name: String?
		get() = "Villager"

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 20
	}

	override var isBaby: Boolean
		get() = getDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_BABY)
		set(baby) {
			this.setDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_BABY, baby)
			setScale(if (baby) 0.5f else 1)
		}

	companion object {
		const val networkId = 115
	}
}