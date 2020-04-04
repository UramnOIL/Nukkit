package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemGlowstoneDust
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.level.generator
import cn.nukkit.math.MathHelper
import cn.nukkit.utils.BlockColor
import java.util.*

/**
 * Created on 2015/12/6 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockGlowstone : BlockTransparent() {
	override val name: String
		get() = "Glowstone"

	override val id: Int
		get() = BlockID.Companion.GLOWSTONE_BLOCK

	override val resistance: Double
		get() = 1.5

	override val hardness: Double
		get() = 0.3

	override val lightLevel: Int
		get() = 15

	override fun getDrops(item: Item): Array<Item?> {
		val random = Random()
		var count = 2 + random.nextInt(3)
		val fortune = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)
		if (fortune != null && fortune.level >= 1) {
			count += random.nextInt(fortune.level + 1)
		}
		return arrayOf(
				ItemGlowstoneDust(0, MathHelper.clamp(count, 1, 4))
		)
	}

	override val color: BlockColor
		get() = BlockColor.SAND_BLOCK_COLOR

	override fun canSilkTouch(): Boolean {
		return true
	}
}