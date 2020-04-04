package cn.nukkit.entity.passive

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Author: BeYkeRYkt Nukkit Project
 */
class EntityOcelot(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {
	override val width: Float
		get() = if (this.isBaby) {
			0.3f
		} else 0.6f

	override val height: Float
		get() = if (this.isBaby) {
			0.35f
		} else 0.7f

	override val name: String?
		get() = "Ocelot"

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 10
	}

	override fun isBreedingItem(item: Item): Boolean {
		return item.id == Item.RAW_FISH
	}

	companion object {
		const val networkId = 22
	}
}