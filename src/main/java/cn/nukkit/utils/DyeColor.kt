package cn.nukkit.utils

enum class DyeColor(val dyeData: Int, val woolData: Int, override val name: String, val dyeName: String, val color: BlockColor) {
	BLACK(0, 15, "Black", "Ink Sack", BlockColor.BLACK_BLOCK_COLOR), RED(1, 14, "Red", "Rose Red", BlockColor.RED_BLOCK_COLOR), GREEN(2, 13, "Green", "Cactus Green", BlockColor.GREEN_BLOCK_COLOR), BROWN(3, 12, "Brown", "Cocoa Beans", BlockColor.BROWN_BLOCK_COLOR), BLUE(4, 11, "Blue", "Lapis Lazuli", BlockColor.BLUE_BLOCK_COLOR), PURPLE(5, 10, "Purple", BlockColor.PURPLE_BLOCK_COLOR), CYAN(6, 9, "Cyan", BlockColor.CYAN_BLOCK_COLOR), LIGHT_GRAY(7, 8, "Light Gray", BlockColor.LIGHT_GRAY_BLOCK_COLOR), GRAY(8, 7, "Gray", BlockColor.GRAY_BLOCK_COLOR), PINK(9, 6, "Pink", BlockColor.PINK_BLOCK_COLOR), LIME(10, 5, "Lime", BlockColor.LIME_BLOCK_COLOR), YELLOW(11, 4, "Yellow", "Dandelion Yellow", BlockColor.YELLOW_BLOCK_COLOR), LIGHT_BLUE(12, 3, "Light Blue", BlockColor.LIGHT_BLUE_BLOCK_COLOR), MAGENTA(13, 2, "Magenta", BlockColor.MAGENTA_BLOCK_COLOR), ORANGE(14, 1, "Orange", BlockColor.ORANGE_BLOCK_COLOR), WHITE(15, 0, "White", "Bone Meal", BlockColor.WHITE_BLOCK_COLOR);

	internal constructor(dyeColorMeta: Int, woolColorMeta: Int, colorName: String, blockColor: BlockColor) : this(dyeColorMeta, woolColorMeta, colorName, "$colorName Dye", blockColor) {}

	companion object {
		private val BY_WOOL_DATA: Array<DyeColor>
		private val BY_DYE_DATA: Array<DyeColor>
		fun getByDyeData(dyeColorMeta: Int): DyeColor {
			return BY_DYE_DATA[dyeColorMeta and 0x0f]
		}

		fun getByWoolData(woolColorMeta: Int): DyeColor {
			return BY_WOOL_DATA[woolColorMeta and 0x0f]
		}

		init {
			BY_DYE_DATA = values()
			BY_WOOL_DATA = values()
			for (color in values()) {
				BY_WOOL_DATA[cn.nukkit.utils.color.woolColorMeta and 0x0f] = cn.nukkit.utils.color
				BY_DYE_DATA[cn.nukkit.utils.color.dyeColorMeta and 0x0f] = cn.nukkit.utils.color
			}
		}
	}

}