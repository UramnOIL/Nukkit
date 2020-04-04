package cn.nukkit.entity.passive

import cn.nukkit.item.Item
import cn.nukkit.item.ItemDye
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.DyeColor

/**
 * @author PikyCZ
 */
class EntitySquid(chunk: FullChunk?, nbt: CompoundTag?) : EntityWaterAnimal(chunk, nbt) {

	override val width: Float
		get() = 0.8f

	override val height: Float
		get() = 0.8f

	public override fun initEntity() {
		super.initEntity()
		maxHealth = 10
	}

	override val drops: Array<Item?>
		get() = arrayOf(ItemDye(DyeColor.BLACK.dyeData))

	companion object {
		const val networkId = 17
	}
}