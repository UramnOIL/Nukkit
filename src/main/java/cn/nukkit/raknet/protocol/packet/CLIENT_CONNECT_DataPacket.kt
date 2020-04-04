package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class CLIENT_CONNECT_DataPacket : Packet() {

	var clientID: Long = 0
	var sendPing: Long = 0
	var useSecurity = false
	override fun encode() {
		super.encode()
		putLong(clientID)
		putLong(sendPing)
		putByte((if (useSecurity) 1 else 0).toByte())
	}

	override fun decode() {
		super.decode()
		clientID = this.long
		sendPing = this.long
		useSecurity = this.byte > 0
	}

	class Factory : PacketFactory {
		override fun create(): Packet {
			return CLIENT_CONNECT_DataPacket()
		}
	}

	companion object {
		const val iD = 0x09.toByte()
	}
}