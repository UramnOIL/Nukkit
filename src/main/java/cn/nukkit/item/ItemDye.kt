package cn.nukkit.item

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.DyeColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ItemDye @JvmOverloads constructor(meta: Int? = 0, amount: Int = 1) : Item(ItemID.Companion.DYE, meta, amount, DyeColor.getByDyeData(meta!!).dyeName) {
	constructor(dyeColor: DyeColor) : this(dyeColor.dyeData, 1) {}
	constructor(dyeColor: DyeColor, amount: Int) : this(dyeColor.dyeData, amount) {}

	val dyeColor: DyeColor
		get() = DyeColor.getByDyeData(meta)

	companion object {
		@Deprecated("")
		val WHITE = DyeColor.WHITE.dyeData

		@Deprecated("")
		val ORANGE = DyeColor.ORANGE.dyeData

		@Deprecated("")
		val MAGENTA = DyeColor.MAGENTA.dyeData

		@Deprecated("")
		val LIGHT_BLUE = DyeColor.LIGHT_BLUE.dyeData

		@Deprecated("")
		val YELLOW = DyeColor.YELLOW.dyeData

		@Deprecated("")
		val LIME = DyeColor.LIME.dyeData

		@Deprecated("")
		val PINK = DyeColor.PINK.dyeData

		@Deprecated("")
		val GRAY = DyeColor.GRAY.dyeData

		@Deprecated("")
		val LIGHT_GRAY = DyeColor.LIGHT_GRAY.dyeData

		@Deprecated("")
		val CYAN = DyeColor.CYAN.dyeData

		@Deprecated("")
		val PURPLE = DyeColor.PURPLE.dyeData

		@Deprecated("")
		val BLUE = DyeColor.BLUE.dyeData

		@Deprecated("")
		val BROWN = DyeColor.BROWN.dyeData

		@Deprecated("")
		val GREEN = DyeColor.GREEN.dyeData

		@Deprecated("")
		val RED = DyeColor.RED.dyeData

		@Deprecated("")
		val BLACK = DyeColor.BLACK.dyeData

		@Deprecated("")
		fun getColor(meta: Int): BlockColor {
			return DyeColor.getByDyeData(meta).color
		}

		@Deprecated("")
		fun getColorName(meta: Int): String {
			return DyeColor.getByDyeData(meta).name
		}
	}

	init {
		if (this.meta == DyeColor.BROWN.dyeData) {
			block = Block[BlockID.COCOA_BLOCK]
		}
	}
}