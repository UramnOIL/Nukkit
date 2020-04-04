package cn.nukkit.entity.mob

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntitySlime(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt) {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 16
	}

	override val width: Float
		get() = 2.04f

	override val height: Float
		get() = 2.04f

	override val name: String?
		get() = "Slime"

	override val drops: Array<Item?>
		get() = arrayOf(Item.get(Item.SLIMEBALL))

	companion object {
		const val networkId = 37
	}
}