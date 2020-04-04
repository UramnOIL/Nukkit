package cn.nukkit.entity.passive

import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityAgeable
import cn.nukkit.entity.EntityCreature
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Created by Pub4Game on 21.06.2016.
 */
class EntityVillagerV1(chunk: FullChunk?, nbt: CompoundTag?) : EntityCreature(chunk, nbt), EntityNPC, EntityAgeable {
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
		if (!namedTag!!.contains("Profession")) {
			profession = PROFESSION_GENERIC
		}
	}

	var profession: Int
		get() = namedTag!!.getInt("Profession")
		set(profession) {
			namedTag!!.putInt("Profession", profession)
		}

	override val isBaby: Boolean
		get() = getDataFlag(Entity.Companion.DATA_FLAGS, Entity.Companion.DATA_FLAG_BABY)

	companion object {
		const val PROFESSION_FARMER = 0
		const val PROFESSION_LIBRARIAN = 1
		const val PROFESSION_PRIEST = 2
		const val PROFESSION_BLACKSMITH = 3
		const val PROFESSION_BUTCHER = 4
		const val PROFESSION_GENERIC = 5
		const val networkId = 15
	}
}