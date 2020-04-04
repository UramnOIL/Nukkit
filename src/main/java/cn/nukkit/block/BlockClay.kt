package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemClay
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * @author Nukkit Project Team
 */
class BlockClay : BlockSolid() {
	override val hardness: Double
		get() = 0.6

	override val resistance: Double
		get() = 3

	override val toolType: Int
		get() = ItemTool.TYPE_SHOVEL

	override val id: Int
		get() = BlockID.Companion.CLAY_BLOCK

	override val name: String
		get() = "Clay Block"

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				ItemClay(0, 4)
		)
	}

	override val color: BlockColor
		get() = BlockColor.CLAY_BLOCK_COLOR

	override fun canSilkTouch(): Boolean {
		return true
	}
}