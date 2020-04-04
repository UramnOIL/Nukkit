package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.protocol.DataPacket
import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class DATA_PACKET_4 : DataPacket() {

	class Factory : PacketFactory {
		override fun create(): Packet {
			return DATA_PACKET_4()
		}
	}

	companion object {
		const val iD = 0x84.toByte()
	}
}