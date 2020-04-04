package cn.nukkit.event.block

import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

/**
 * Created by Snake1999 on 2016/1/22.
 * Package cn.nukkit.event.block in project nukkit.
 */
class DoorToggleEvent(block: Block?, var player: Player) : BlockUpdateEvent(block), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}