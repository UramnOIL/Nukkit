package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created by Pub4Game on 26.12.2015.
 */
class BlockTrapdoorIron @JvmOverloads constructor(meta: Int = 0) : BlockTrapdoor(meta) {
	override val id: Int
		get() = BlockID.Companion.IRON_TRAPDOOR

	override val name: String
		get() = "Iron Trapdoor"

	override val hardness: Double
		get() = 5

	override val resistance: Double
		get() = 25

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val color: BlockColor
		get() = BlockColor.IRON_BLOCK_COLOR

	override fun onActivate(item: Item, player: Player?): Boolean {
		return false
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}