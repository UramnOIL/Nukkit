package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor

/*
 * Created on 2015/12/11 by Pub4Game.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockRedstone @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(0) {
	override val id: Int
		get() = BlockID.Companion.REDSTONE_BLOCK

	override val resistance: Double
		get() = 10

	override val hardness: Double
		get() = 5

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Redstone Block"

	//TODO: redstone
	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(
					toItem()
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val color: BlockColor
		get() = BlockColor.REDSTONE_BLOCK_COLOR

	override val isPowerSource: Boolean
		get() = true

	override fun getWeakPower(face: BlockFace): Int {
		return 15
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}
}