package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemDye
import cn.nukkit.item.ItemTool
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BlockOreLapis : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.LAPIS_ORE

	override val hardness: Double
		get() = 3

	override val resistance: Double
		get() = 5

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Lapis Lazuli Ore"

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_STONE) {
			var count = 4 + ThreadLocalRandom.current().nextInt(5)
			val fortune = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)
			if (fortune != null && fortune.level >= 1) {
				var i = ThreadLocalRandom.current().nextInt(fortune.level + 2) - 1
				if (i < 0) {
					i = 0
				}
				count *= i + 1
			}
			arrayOf(
					ItemDye(4, Random().nextInt(4) + 4)
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override val dropExp: Int
		get() = NukkitRandom().nextRange(2, 5)

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun canSilkTouch(): Boolean {
		return true
	}
}