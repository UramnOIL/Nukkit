package cn.nukkit.block

import cn.nukkit.event.redstone.RedstoneUpdateEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.Level

/**
 * @author Pub4Game
 */
class BlockRedstoneLampLit : BlockRedstoneLamp() {
	override val name: String
		get() = "Lit Redstone Lamp"

	override val id: Int
		get() = BlockID.Companion.LIT_REDSTONE_LAMP

	override val lightLevel: Int
		get() = 15

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.REDSTONE_LAMP))
	}

	override fun onUpdate(type: Int): Int {
		if ((type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_REDSTONE) && !level.isBlockPowered(this.location)) {
			// Redstone event
			val ev = RedstoneUpdateEvent(this)
			getLevel().server.pluginManager.callEvent(ev)
			if (ev.isCancelled) {
				return 0
			}
			level.scheduleUpdate(this, 4)
			return 1
		}
		if (type == Level.BLOCK_UPDATE_SCHEDULED && !level.isBlockPowered(this.location)) {
			level.setBlock(this, Block.Companion.get(BlockID.Companion.REDSTONE_LAMP), false, false)
		}
		return 0
	}
}