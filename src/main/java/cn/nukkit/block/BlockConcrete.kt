package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.DyeColor

/**
 * Created by CreeperFace on 2.6.2017.
 */
class BlockConcrete @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.CONCRETE

	override val resistance: Double
		get() = 9

	override val hardness: Double
		get() = 1.8

	override val name: String
		get() = "Concrete"

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.tier >= ItemTool.TIER_WOODEN) arrayOf(toItem()) else arrayOfNulls(0)
	}

	override val color: BlockColor
		get() = DyeColor.getByWoolData(damage).color

	val dyeColor: DyeColor
		get() = DyeColor.getByWoolData(damage)
}