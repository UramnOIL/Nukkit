package cn.nukkit.entity.passive

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityPolarBear(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {

	override val width: Float
		get() = if (this.isBaby) {
			0.65f
		} else 1.3f

	override val height: Float
		get() = if (this.isBaby) {
			0.7f
		} else 1.4f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 30
	}

	override val drops: Array<Item?>
		get() = arrayOf(Item.get(Item.RAW_FISH), Item.get(Item.RAW_SALMON))

	companion object {
		const val networkId = 28
	}
}