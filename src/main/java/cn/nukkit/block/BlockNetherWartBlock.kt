package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

class BlockNetherWartBlock : BlockSolid() {
	override val name: String
		get() = "Nether Wart Block"

	override val id: Int
		get() = BlockID.Companion.BLOCK_NETHER_WART_BLOCK

	override val resistance: Double
		get() = 5

	override val hardness: Double
		get() = 1

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				toItem()
		)
	}

	override val color: BlockColor
		get() = BlockColor.RED_BLOCK_COLOR
}