package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: Angelic47
 * Nukkit Project
 */
class BlockLapis : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.LAPIS_BLOCK

	override val name: String
		get() = "Lapis Lazuli Block"

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val hardness: Double
		get() = 3

	override val resistance: Double
		get() = 5

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_STONE) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val color: BlockColor
		get() = BlockColor.LAPIS_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}