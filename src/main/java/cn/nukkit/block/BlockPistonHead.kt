package cn.nukkit.block

import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace

/**
 * @author CreeperFace
 */
class BlockPistonHead @JvmOverloads constructor(meta: Int = 0) : BlockTransparentMeta(meta) {
	override val id: Int
		get() = BlockID.Companion.PISTON_HEAD

	override val name: String
		get() = "Piston Head"

	override val resistance: Double
		get() = 2.5

	override val hardness: Double
		get() = 0.5

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOfNulls(0)
	}

	override fun onBreak(item: Item): Boolean {
		level.setBlock(this, Block.Companion.get(BlockID.Companion.AIR), true, true)
		val piston = getSide(facing.opposite)
		if (piston is BlockPistonBase && piston.facing == facing) {
			piston.onBreak(item)
		}
		return true
	}

	val facing: BlockFace
		get() = BlockFace.fromIndex(this.damage).opposite

	override fun canBePushed(): Boolean {
		return false
	}

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.AIR))
	}
}