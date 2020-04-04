package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemMelon
import cn.nukkit.item.ItemTool
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor
import java.util.*

/**
 * Created on 2015/12/11 by Pub4Game.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockMelon : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.MELON_BLOCK

	override val name: String
		get() = "Melon Block"

	override val hardness: Double
		get() = 1

	override val resistance: Double
		get() = 5

	override fun getDrops(item: Item): Array<Item?> {
		val random = Random()
		var count = 3 + random.nextInt(5)
		val fortune = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)
		if (fortune != null && fortune.level >= 1) {
			count += random.nextInt(fortune.level + 1)
		}
		return arrayOf(
				ItemMelon(0, Math.min(9, count))
		)
	}

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override val color: BlockColor
		get() = BlockColor.LIME_BLOCK_COLOR

	override fun canSilkTouch(): Boolean {
		return true
	}
}