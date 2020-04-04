package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.Faceable

/**
 * Created by CreeperFace on 2.6.2017.
 */
abstract class BlockTerracottaGlazed @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta), Faceable {
	override val resistance: Double
		get() = 7

	override val hardness: Double
		get() = 1.4

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun getDrops(item: Item): Array<Item?> {
		return if (item.tier >= ItemTool.TIER_WOODEN) arrayOf(toItem()) else arrayOfNulls(0)
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		val faces = intArrayOf(2, 5, 3, 4)
		this.setDamage(faces[player?.direction?.horizontalIndex ?: 0])
		return getLevel().setBlock(block, this, true, true)
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x07)
	}
}