package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created by CreeperFace on 26. 11. 2016.
 */
class BlockStairsRedSandstone @JvmOverloads constructor(meta: Int = 0) : BlockStairs(meta) {
	override val id: Int
		get() = BlockID.Companion.RED_SANDSTONE_STAIRS

	override val hardness: Double
		get() = 0.8

	override val resistance: Double
		get() = 4

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Red Sandstone Stairs"

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun toItem(): Item? {
		return ItemBlock(this, this.damage and 0x07)
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override val color: BlockColor
		get() = BlockColor.ORANGE_BLOCK_COLOR
}