package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor

/**
 * Created on 2015/11/25 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
open class BlockStairsWood @JvmOverloads constructor(meta: Int = 0) : BlockStairs(meta) {
	override val id: Int
		get() = BlockID.Companion.WOOD_STAIRS

	override val name: String
		get() = "Wood Stairs"

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override val hardness: Double
		get() = 2

	override val resistance: Double
		get() = 15

	override val burnChance: Int
		get() = 5

	override val burnAbility: Int
		get() = 20

	override val color: BlockColor
		get() = BlockColor.WOOD_BLOCK_COLOR

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				toItem()
		)
	}
}