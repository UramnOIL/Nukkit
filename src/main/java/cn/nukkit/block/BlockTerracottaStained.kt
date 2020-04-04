package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.DyeColor

/**
 * Created on 2015/12/2 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockTerracottaStained @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	constructor(dyeColor: DyeColor) : this(dyeColor.woolData) {}

	override val name: String
		get() = dyeColor.name + " Terracotta"

	override val id: Int
		get() = BlockID.Companion.STAINED_TERRACOTTA

	override val hardness: Double
		get() = 1.25

	override val resistance: Double
		get() = 0.75

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			arrayOf(toItem())
		} else {
			arrayOfNulls(0)
		}
	}

	override val color: BlockColor
		get() = DyeColor.getByWoolData(damage).color

	val dyeColor: DyeColor
		get() = DyeColor.getByWoolData(damage)
}