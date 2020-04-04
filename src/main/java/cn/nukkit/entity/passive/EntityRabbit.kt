package cn.nukkit.entity.passive

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Author: BeYkeRYkt Nukkit Project
 */
class EntityRabbit(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {
	override val width: Float
		get() = if (this.isBaby) {
			0.2f
		} else 0.4f

	override val height: Float
		get() = if (this.isBaby) {
			0.25f
		} else 0.5f

	override val name: String?
		get() = "Rabbit"

	override val drops: Array<Item?>
		get() = arrayOf(Item.get(Item.RAW_RABBIT), Item.get(Item.RABBIT_HIDE), Item.get(Item.RABBIT_FOOT))

	override fun initEntity() {
		super.initEntity()
		maxHealth = 10
	}

	companion object {
		const val networkId = 18
	}
}