package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemFlint
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockGravel : BlockFallable() {
	override val id: Int
		get() = BlockID.Companion.GRAVEL

	override val hardness: Double
		get() = 0.6

	override val resistance: Double
		get() = 3

	override val toolType: Int
		get() = ItemTool.TYPE_SHOVEL

	override val name: String
		get() = "Gravel"

	override fun getDrops(item: Item): Array<Item?> {
		return if (Random().nextInt(9) == 0) {
			arrayOf(
					ItemFlint()
			)
		} else {
			arrayOf(
					toItem()
			)
		}
	}

	override fun canSilkTouch(): Boolean {
		return true
	}
}