package cn.nukkit.event.server

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.network.protocol.DataPacket

class BatchPacketsEvent(val players: Array<Player>, val packets: Array<DataPacket>, val isForceSync: Boolean) : ServerEvent(), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}