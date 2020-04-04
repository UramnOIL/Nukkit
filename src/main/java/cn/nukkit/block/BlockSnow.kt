package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemSnowball
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

class BlockSnow : BlockSolid() {
	override val name: String
		get() = "Snow Block"

	override val id: Int
		get() = BlockID.Companion.SNOW_BLOCK

	override val hardness: Double
		get() = 0.2

	override val resistance: Double
		get() = 1

	override val toolType: Int
		get() = ItemTool.TYPE_SHOVEL

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isShovel && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					ItemSnowball(0, 4)
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val color: BlockColor
		get() = BlockColor.SNOW_BLOCK_COLOR

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun canSilkTouch(): Boolean {
		return true
	}
}