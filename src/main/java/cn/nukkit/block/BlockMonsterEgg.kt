package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.level.generator

class BlockMonsterEgg @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.MONSTER_EGG

	override val hardness: Double
		get() = 0.75

	override val resistance: Double
		get() = 3.75

	override val name: String
		get() = NAMES[if (this.damage > 5) 0 else this.damage] + " Monster Egg"

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOfNulls(0)
	}

	companion object {
		const val STONE = 0
		const val COBBLESTONE = 1
		const val STONE_BRICK = 2
		const val MOSSY_BRICK = 3
		const val CRACKED_BRICK = 4
		const val CHISELED_BRICK = 5
		private val NAMES = arrayOf(
				"Stone",
				"Cobblestone",
				"Stone Brick",
				"Mossy Stone Brick",
				"Cracked Stone Brick",
				"Chiseled Stone Brick"
		)
	}
}