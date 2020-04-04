package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemQuartz
import cn.nukkit.item.ItemTool
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom
import java.util.concurrent.ThreadLocalRandom

/**
 * Created on 2015/12/26 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockOreQuartz : BlockSolid() {
	override val name: String
		get() = "Quartz Ore"

	override val id: Int
		get() = BlockID.Companion.QUARTZ_ORE

	override val hardness: Double
		get() = 3

	override val resistance: Double
		get() = 5

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_WOODEN) {
			var count = 1
			val fortune = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)
			if (fortune != null && fortune.level >= 1) {
				var i = ThreadLocalRandom.current().nextInt(fortune.level + 2) - 1
				if (i < 0) {
					i = 0
				}
				count = i + 1
			}
			arrayOf(
					ItemQuartz(0, count)
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val dropExp: Int
		get() = NukkitRandom().nextRange(1, 5)

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun canSilkTouch(): Boolean {
		return true
	}
}