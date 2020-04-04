package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.TerracottaColor

/**
 * Created on 2015/11/24 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockTerracotta @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(0) {
	constructor(dyeColor: TerracottaColor) : this(dyeColor.terracottaData) {}

	override val id: Int
		get() = BlockID.Companion.TERRACOTTA

	override val name: String
		get() = "Terracotta"

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val hardness: Double
		get() = 1.25

	override val resistance: Double
		get() = 7

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
		get() = TerracottaColor.getByTerracottaData(damage).color

	val dyeColor: TerracottaColor
		get() = TerracottaColor.getByTerracottaData(damage)
}