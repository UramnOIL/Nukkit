package cn.nukkit.entity.passive

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Author: BeYkeRYkt Nukkit Project
 */
class EntityCow(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {
	override val width: Float
		get() = if (this.isBaby) {
			0.45f
		} else 0.9f

	override val height: Float
		get() = if (this.isBaby) {
			0.7f
		} else 1.4f

	override val name: String?
		get() = "Cow"

	override val drops: Array<Item?>
		get() = arrayOf(Item.get(Item.LEATHER), Item.get(Item.RAW_BEEF))

	override fun initEntity() {
		super.initEntity()
		maxHealth = 10
	}

	companion object {
		const val networkId = 11
	}
}