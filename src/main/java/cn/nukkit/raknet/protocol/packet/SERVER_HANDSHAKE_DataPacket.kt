package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.protocol.Packet
import java.net.InetSocketAddress

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class SERVER_HANDSHAKE_DataPacket : Packet() {

	override var address: String? = null
	var port = 0
	val systemAddresses = arrayOf(
			InetSocketAddress("127.0.0.1", 0),
			InetSocketAddress("0.0.0.0", 0),
			InetSocketAddress("0.0.0.0", 0),
			InetSocketAddress("0.0.0.0", 0),
			InetSocketAddress("0.0.0.0", 0),
			InetSocketAddress("0.0.0.0", 0),
			InetSocketAddress("0.0.0.0", 0),
			InetSocketAddress("0.0.0.0", 0),
			InetSocketAddress("0.0.0.0", 0),
			InetSocketAddress("0.0.0.0", 0)
	)
	var sendPing: Long = 0
	var sendPong: Long = 0
	override fun encode() {
		super.encode()
		this.putAddress(InetSocketAddress(address, port))
		putShort(0)
		for (i in 0..9) {
			this.putAddress(systemAddresses[i])
		}
		putLong(sendPing)
		putLong(sendPong)
	}

	class Factory : PacketFactory {
		override fun create(): Packet {
			return SERVER_HANDSHAKE_DataPacket()
		}
	}

	companion object {
		const val iD = 0x10.toByte()
	}
}