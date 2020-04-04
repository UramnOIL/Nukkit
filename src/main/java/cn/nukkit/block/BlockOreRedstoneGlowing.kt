package cn.nukkit.block

import cn.nukkit.event.block.BlockFadeEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemBlock
import cn.nukkit.level.Level

//和pm源码有点出入，这里参考了wiki
/**
 * Created on 2015/12/6 by xtypr.
 * Package cn.nukkit.block in project Nukkit .
 */
class BlockOreRedstoneGlowing : BlockOreRedstone() {
	override val name: String
		get() = "Glowing Redstone Ore"

	override val id: Int
		get() = BlockID.Companion.GLOWING_REDSTONE_ORE

	override val lightLevel: Int
		get() = 9

	override fun toItem(): Item? {
		return ItemBlock(Block.Companion.get(BlockID.Companion.REDSTONE_ORE))
	}

	override fun onUpdate(type: Int): Int {
		if (type == Level.BLOCK_UPDATE_SCHEDULED || type == Level.BLOCK_UPDATE_RANDOM) {
			val event = BlockFadeEvent(this, Block.Companion.get(BlockID.Companion.REDSTONE_ORE))
			level.server.pluginManager.callEvent(event)
			if (!event.isCancelled) {
				level.setBlock(this, event.newState, false, false)
			}
			return Level.BLOCK_UPDATE_WEAK
		}
		return 0
	}

	override fun canHarvestWithHand(): Boolean {
		return false
	}

	override fun canSilkTouch(): Boolean {
		return true
	}
}