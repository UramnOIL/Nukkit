package cn.nukkit.block

import cn.nukkit.level.generator
import cn.nukkit.utils.DyeColor

/**
 * Created by CreeperFace on 2.6.2017.
 */
class BlockTerracottaGlazedCyan @JvmOverloads constructor(meta: Int = 0) : BlockTerracottaGlazed(meta) {
	override val id: Int
		get() = BlockID.Companion.CYAN_GLAZED_TERRACOTTA

	override val name: String
		get() = "Cyan Glazed Terracotta"

	val dyeColor: DyeColor
		get() = DyeColor.CYAN
}