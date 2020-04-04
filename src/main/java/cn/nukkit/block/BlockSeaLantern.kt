package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemPrismarineCrystals
import cn.nukkit.level.generator
import cn.nukkit.utils.BlockColor
import java.util.concurrent.ThreadLocalRandom

class BlockSeaLantern : BlockTransparent() {
	override val name: String
		get() = "Sea Lantern"

	override val id: Int
		get() = BlockID.Companion.SEA_LANTERN

	override val resistance: Double
		get() = 1.5

	override val hardness: Double
		get() = 0.3

	override val lightLevel: Int
		get() = 15

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				ItemPrismarineCrystals(0, ThreadLocalRandom.current().nextInt(2, 4))
		)
	}

	override val color: BlockColor
		get() = BlockColor.QUARTZ_BLOCK_COLOR

	override fun canSilkTouch(): Boolean {
		return true
	}
}