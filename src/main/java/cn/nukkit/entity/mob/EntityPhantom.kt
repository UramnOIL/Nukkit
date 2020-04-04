package cn.nukkit.entity.mob

import cn.nukkit.entity.EntitySmite
import cn.nukkit.item.Item
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * Created by PetteriM1
 */
class EntityPhantom(chunk: FullChunk?, nbt: CompoundTag?) : EntityMob(chunk, nbt), EntitySmite {

	override fun initEntity() {
		super.initEntity()
		maxHealth = 20
	}

	override val width: Float
		get() = 0.9f

	override val height: Float
		get() = 0.5f

	override val name: String?
		get() = "Phantom"

	override val drops: Array<Item?>
		get() = arrayOf(Item.get(470))

	companion object {
		const val networkId = 58
	}
}