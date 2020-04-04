package cn.nukkit.entity.passive

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Author: BeYkeRYkt Nukkit Project
 */
class EntityChicken(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {
	override val width: Float
		get() = if (this.isBaby) {
			0.2f
		} else 0.4f

	override val height: Float
		get() = if (this.isBaby) {
			0.35f
		} else 0.7f

	override val name: String?
		get() = "Chicken"

	override val drops: Array<Item?>
		get() = arrayOf(Item.get(Item.RAW_CHICKEN), Item.get(Item.FEATHER))

	override fun initEntity() {
		super.initEntity()
		maxHealth = 4
	}

	override fun isBreedingItem(item: Item): Boolean {
		val id = item.id
		return id == Item.WHEAT_SEEDS || id == Item.MELON_SEEDS || id == Item.PUMPKIN_SEEDS || id == Item.BEETROOT_SEEDS
	}

	companion object {
		const val networkId = 10
	}
}