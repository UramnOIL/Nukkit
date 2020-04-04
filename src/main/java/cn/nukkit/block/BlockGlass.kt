package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * author: Angelic47
 * Nukkit Project
 */
open class BlockGlass : BlockTransparent() {
	override val id: Int
		get() = BlockID.Companion.GLASS

	override val name: String
		get() = "Glass"

	override val resistance: Double
		get() = 1.5

	override val hardness: Double
		get() = 0.3

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOfNulls(0)
	}

	override fun canSilkTouch(): Boolean {
		return true
	}

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR
}