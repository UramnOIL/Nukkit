package cn.nukkit.event.server

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.network.protocol.DataPacket

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class DataPacketReceiveEvent(val player: Player, val packet: DataPacket) : ServerEvent(), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}