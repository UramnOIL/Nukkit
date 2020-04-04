package cn.nukkit.entity.mob

import cn.nukkit.entity.EntityArthropod
import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author PikyCZ
 */
class EntitySpider(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt), EntityArthropod {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 16
	}

	override val width: Float
		get() = 1.4f

	override val height: Float
		get() = 0.9f

	override val name: String?
		get() = "Spider"

	override val drops: Array<Item?>
		get() = arrayOf(Item.get(Item.STRING, Item.SPIDER_EYE))

	companion object {
		const val networkId = 35
	}
}