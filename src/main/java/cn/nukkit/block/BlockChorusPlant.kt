package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

class BlockChorusPlant : BlockTransparent() {
	override val id: Int
		get() = BlockID.Companion.CHORUS_PLANT

	override val name: String
		get() = "Chorus Plant"

	override val hardness: Double
		get() = 0.4

	override val resistance: Double
		get() = 2

	override val toolType: Int
		get() = ItemTool.TYPE_NONE

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOfNulls(0)
	}

	override val color: BlockColor
		get() = BlockColor.PURPLE_BLOCK_COLOR
}