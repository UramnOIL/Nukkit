package cn.nukkit.entity.passive

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityHorse(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {

	override val width: Float
		get() = if (this.isBaby) {
			0.6982f
		} else 1.3965f

	override val height: Float
		get() = if (this.isBaby) {
			0.8f
		} else 1.6f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 15
	}

	override val drops: Array<Item?>
		get() = arrayOf(Item.get(Item.LEATHER))

	companion object {
		const val networkId = 23
	}
}