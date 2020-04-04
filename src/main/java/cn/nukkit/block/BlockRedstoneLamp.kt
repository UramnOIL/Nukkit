package cn.nukkit.block

import cn.nukkit.Player
import cn.nukkit.event.redstone.RedstoneUpdateEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.math.BlockFace
import cn.nukkit.utils.BlockColor

/**
 * @author Nukkit Project Team
 */
open class BlockRedstoneLamp : BlockSolid() {
	override val name: String
		get() = "Redstone Lamp"

	override val id: Int
		get() = BlockID.Companion.REDSTONE_LAMP

	override val hardness: Double
		get() = 0.3

	override val resistance: Double
		get() = 1.5

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun place(item: Item, block: Block, target: Block, face: BlockFace, fx: Double, fy: Double, fz: Double, player: Player?): Boolean {
		if (level.isBlockPowered(this.location)) {
			level.setBlock(this, Block.Companion.get(BlockID.Companion.LIT_REDSTONE_LAMP), false, true)
		} else {
			level.setBlock(this, this, false, true)
		}
		return true
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) {
			// Redstone event
			val ev = RedstoneUpdateEvent(this)
			getLevel().server.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				return 0
			}
			if (level.isBlockPowered(this.location)) {
				level.setBlock(this, Block.Companion.get(BlockID.Companion.LIT_REDSTONE_LAMP), false, false)
				return 1
			}
		}
		return 0
	}

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOf(
				ItemBlock(Block.Companion.get(BlockID.Companion.REDSTONE_LAMP))
		)
	}

	override val color: BlockColor
		get() = BlockColor.AIR_BLOCK_COLOR
}