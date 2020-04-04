package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemRedstone
import cn.nukkit.item.ItemTool
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.level.Level
import cn.nukkit.math.NukkitRandom
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class BlockOreRedstone : BlockSolid() {
	override val id: Int
		get() = BlockID.Companion.REDSTONE_ORE

	override val hardness: Double
		get() = 3

	override val resistance: Double
		get() = 15

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override val name: String
		get() = "Redstone Ore"

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.isPickaxe && item.tier >= ItemTool.TIER_IRON) {
			var count = Random().nextInt(2) + 4
			val fortune = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING)
			if (fortune != null && fortune.level >= 1) {
				count += Random().nextInt(fortune.level + 1)
			}
			arrayOf(
					ItemRedstone(0, count)
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_TOUCH) { //type == Level.BLOCK_UPDATE_NORMAL ||
			getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.GLOWING_REDSTONE_ORE), false, false)
			return Level.BLOCK_UPDATE_WEAK
		}
		return 0
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