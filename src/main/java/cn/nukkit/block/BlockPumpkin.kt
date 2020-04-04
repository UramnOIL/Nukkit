package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor
import cn.nukkit.utils.Faceable

/**
 * Created on 2015/12/8 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
open class BlockPumpkin @JvmOverloads constructor(meta: Int = 0) : BlockSolidMeta(meta), Faceable {
	override val name: String
		get() = "Pumpkin"

	override val id: Int
		get() = BlockID.Companion.PUMPKIN

	override val hardness: Double
		get() = 1

	override val resistance: Double
		get() = 5

	override val toolType: Int
		get() = ItemTool.TYPE_AXE

	override fun toItem(): Item? {
		return ItemBlock(this, 0)
	}

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		this.setDamage(player?.direction?.opposite?.horizontalIndex ?: 0)
		getLevel().setBlock(block, this, true, true)
		return true
	}

	override val color: BlockColor
		get() = BlockColor.ORANGE_BLOCK_COLOR

	override fun canBePushed(): Boolean {
		return false
	}

	override fun getBlockFace(): BlockFace {
		return BlockFace.fromHorizontalIndex(this.damage and 0x7)
	}
}