package cn.nukkit.entity.mob

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntityVindicator(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt) {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 24
	}

	override val width: Float
		get() = 0.6f

	override val height: Float
		get() = 1.95f

	override val name: String?
		get() = "Vindicator"

	override val drops: Array<Item?>
		get() = arrayOf(Item.get(Item.IRON_AXE))

	companion object {
		const val networkId = 57
	}
}