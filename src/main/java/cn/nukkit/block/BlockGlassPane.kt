package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/12/6 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
open class BlockGlassPane : BlockThin() {
	override val name: String
		get() = "Glass Pane"

	override val id: Int
		get() = BlockID.Companion.GLASS_PANE

	override val resistance: Double
		get() = 1.5

	override val hardness: Double
		get() = 0.3

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOfNulls(0)
	}

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR

	override fun canSilkTouch(): Boolean {
		return true
	}
}