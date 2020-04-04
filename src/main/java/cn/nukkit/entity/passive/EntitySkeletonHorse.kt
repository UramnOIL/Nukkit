package cn.nukkit.entity.passive

import cn.nukkit.entity.EntitySmite
import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntitySkeletonHorse(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt), EntitySmite {

	override val width: Float
		get() = 1.4f

	override val height: Float
		get() = 1.6f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 15
	}

	override val drops: Array<Item?>
		get() = arrayOf(Item.get(Item.BONE))

	companion object {
		const val networkId = 26
	}
}