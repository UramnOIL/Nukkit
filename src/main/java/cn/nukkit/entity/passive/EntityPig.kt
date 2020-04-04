package cn.nukkit.entity.passive

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Author: BeYkeRYkt Nukkit Project
 */
class EntityPig(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {
	override val width: Float
		get() = if (this.isBaby) {
			0.45f
		} else 0.9f

	override val height: Float
		get() = if (this.isBaby) {
			0.45f
		} else 0.9f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 10
	}

	override val name: String?
		get() = "Pig"

	override val drops: Array<Item?>
		get() = arrayOf(Item.get(Item.RAW_PORKCHOP))

	override fun isBreedingItem(item: Item): Boolean {
		val id = item.id
		return id == Item.CARROT || id == Item.POTATO || id == Item.BEETROOT
	}

	companion object {
		const val networkId = 12
	}
}