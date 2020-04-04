package cn.nukkit.entity.passive

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityMule(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {
	override val drops: Array<Item?>
		get() = arrayOf(Item.get(Item.LEATHER))

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

	companion object {
		const val networkId = 25
	}
}