package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBook
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * @author Nukkit Project Team
 */
class BlockBookshelf @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val name: String
		get() = "Bookshelf"

	override val id: Int
		get() = BlockID.Companion.BOOKSHELF

	override val hardness: Double
		get() = 1.5

	override val resistance: Double
		get() = 7.5

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override val burnChance: Int
		get() = 30

	override val burnAbility: Int
		get() = 20

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				ItemBook(0, 3)
		)
	}

	override val color: BlockColor
		get() = BlockColor.WOOD_BLOCK_COLOR

	override fun canSilkTouch(): Boolean {
		return true
	}
}