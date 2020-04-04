package cn.nukkit.event.block

import cn.nukkit.block.Block
import cn.nukkit.event.HandlerList

/**
 * Created by CreeperFace on 2.8.2017.
 */
class BlockPistonChangeEvent(block: Block?, val oldPower: Int, val newPower: Int) : BlockEvent(block) {

	companion object {
		val handlers = HandlerList()
	}

}