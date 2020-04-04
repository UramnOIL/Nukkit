package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class UNCONNECTED_PING_OPEN_CONNECTIONS : UNCONNECTED_PING() {

	class Factory : PacketFactory {
		override fun create(): Packet {
			return UNCONNECTED_PING_OPEN_CONNECTIONS()
		}
	}

	companion object {
		const val iD = 0x02.toByte()
	}
}