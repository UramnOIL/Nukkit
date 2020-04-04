package cn.nukkit.block

import cn.nukkit.event.block.BlockFadeEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemTool
import cn.nukkit.level.Level
import cn.nukkit.utils.BlockColor

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class BlockIce : BlockTransparent() {
	override val id: Int
		get() = BlockID.Companion.ICE

	override val name: String
		get() = "Ice"

	override val resistance: Double
		get() = 2.5

	override val hardness: Double
		get() = 0.5

	override val frictionFactor: Double
		get() = 0.98

	override val toolType: Int
		get() = ItemTool.TYPE_PICKAXE

	override fun onBreak(item: Item): Boolean {
		getLevel().setBlock(this, Block.Companion.get(BlockID.Companion.WATER), true)
		return true
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_RANDOM) {
			if (getLevel().getBlockLightAt(x.toInt(), y.toInt(), z.toInt()) >= 12) {
				val event = BlockFadeEvent(this, Block.Companion.get(BlockID.Companion.WATER))
				level.server.pluginManager.callEvent(event)
				if (!event.isCancelled) {
					level.setBlock(this, event.newState, true)
				}
				return Level.BLOCK_UPDATE_NORMAL
			}
		}
		return 0
	}

	override fun getDrops(item: Item): Array<Item?> {
		return arrayOfNulls(0)
	}

	override val color: BlockColor
		get() = BlockColor.ICE_BLOCK_COLOR

	override fun canSilkTouch(): Boolean {
		return true
	}
}