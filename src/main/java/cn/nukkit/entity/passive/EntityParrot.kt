package cn.nukkit.entity.passive

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityParrot(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {

	override val name: String?
		get() = "Parrot"

	override val width: Float
		get() = 0.5f

	override val height: Float
		get() = 0.9f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 6
	}

	override val drops: Array<Item?>
		get() = arrayOf(Item.get(Item.FEATHER))

	companion object {
		const val networkId = 30
	}
}