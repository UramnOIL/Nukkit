package cn.nukkit.block

import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.DyeColor

/**
 * Created on 2015/12/2 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockWool @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	constructor(dyeColor: DyeColor) : this(dyeColor.woolData) {}

	override val name: String
		get() = dyeColor.name + " Wool"

	override val id: Int
		get() = BlockID.Companion.WOOL

	override val toolType: Int
		get() = ItemTool.TYPE_SHEARS

	override val hardness: Double
		get() = 0.8

	override val resistance: Double
		get() = 4

	override val burnChance: Int
		get() = 30

	override val burnAbility: Int
		get() = 60

	override val color: BlockColor
		get() = DyeColor.getByWoolData(damage).color

	val dyeColor: DyeColor
		get() = DyeColor.getByWoolData(damage)
}