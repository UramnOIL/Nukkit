package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom
import cn.nukkit.utils.BlockColor

/**
 * Created by Pub4Game on 28.01.2016.
 */
class BlockHugeMushroomBrown @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta) {
	override val name: String
		get() = "Brown Mushroom Block"

	override val id: Int
		get() = BlockID.Companion.BROWN_MUSHROOM_BLOCK

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override val hardness: Double
		get() = 0.2

	override val resistance: Double
		get() = 1

	override fun getDrops(item: Item): Array<Item?> {
		return if (NukkitRandom().nextRange(1, 20) == 0) {
			arrayOf(
					ItemBlock(Block.Companion.get(BlockID.Companion.BROWN_MUSHROOM))
			)
		} else {
			arrayOfNulls(0)
		}
	}

	override fun canSilkTouch(): Boolean {
		return true
	}

	override val color: BlockColor
		get() = BlockColor.DIRT_BLOCK_COLOR
}