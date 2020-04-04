package cn.nukkit.entity.passive

import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Created by PetteriM1
 */
class EntityDolphin(chunk: FullChunk?, nbt: CompoundTag?) : EntityAnimal(chunk, nbt) {

	override val name: String?
		get() = "Dolphin"

	override val width: Float
		get() = 0.9f

	override val height: Float
		get() = 0.6f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 10
	}

	override val drops: Array<Item?>
		get() = arrayOf(Item.get(Item.RAW_FISH))

	companion object {
		const val networkId = 31
	}
}