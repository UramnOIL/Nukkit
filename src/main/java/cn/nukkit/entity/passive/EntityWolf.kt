package cn.nukkit.entity.passive

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Author: BeYkeRYkt Nukkit Project
 */
class EntityWolf(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {
	override val width: Float
		get() = 0.6f

	override val height: Float
		get() = 0.85f

	override val name: String?
		get() = "Wolf"

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 8
	}

	override fun isBreedingItem(item: Item): Boolean {
		return false //only certain food
	}

	companion object {
		const val networkId = 14
	}
}