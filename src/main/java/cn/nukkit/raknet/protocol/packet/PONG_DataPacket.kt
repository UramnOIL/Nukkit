package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PONG_DataPacket : Packet() {

	var pingID: Long = 0
	override fun encode() {
		super.encode()
		putLong(pingID)
	}

	override fun decode() {
		super.decode()
		pingID = this.long
	}

	class Factory : PacketFactory {
		override fun create(): Packet {
			return PONG_DataPacket()
		}
	}

	companion object {
		const val iD = 0x03.toByte()
	}
}