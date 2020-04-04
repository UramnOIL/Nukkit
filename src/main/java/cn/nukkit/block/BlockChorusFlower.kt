package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator

class BlockChorusFlower : BlockTransparent() {
	override val id: Int
		get() = BlockID.Companion.CHORUS_FLOWER

	override val name: String
		get() = "Chorus Flower"

	override val hardness: Double
		get() = 0.4

	override val resistance: Double
		get() = 2

	override val toolType: Int
		get() = ItemTool.TYPE_NONE

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOfNulls(0)
	}
}