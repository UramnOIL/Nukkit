package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ADVERTISE_SYSTEM : UNCONNECTED_PONG() {

	class Factory : PacketFactory {
		override fun create(): Packet {
			return ADVERTISE_SYSTEM()
		}
	}

	companion object {
		const val iD = 0x1d.toByte()
	}
}