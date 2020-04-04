package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class CLIENT_DISCONNECT_DataPacket : Packet() {

	class Factory : PacketFactory {
		override fun create(): Packet {
			return CLIENT_DISCONNECT_DataPacket()
		}
	}

	companion object {
		const val iD = 0x15.toByte()
	}
}