package cn.nukkit.event.block

import cn.nukkit.block.Block
import cn.nukkit.block.BlockLiquid
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class LiquidFlowEvent(val to: Block?, val source: BlockLiquid, val newFlowDecay: Int) : BlockEvent(to), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}