package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.protocol.Packet
import java.net.InetSocketAddress

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class CLIENT_HANDSHAKE_DataPacket : Packet() {

	override var address: String? = null
	var port = 0
	val systemAddresses = arrayOfNulls<InetSocketAddress>(10)
	var sendPing: Long = 0
	var sendPong: Long = 0
	override fun encode() {}
	override fun decode() {
		super.decode()
		val addr = getAddress()
		address = addr!!.hostString
		port = addr!!.port
		for (i in 0..9) {
			systemAddresses[i] = getAddress()
		}
		sendPing = this.long
		sendPong = this.long
	}

	class Factory : PacketFactory {
		override fun create(): Packet {
			return CLIENT_HANDSHAKE_DataPacket()
		}
	}

	companion object {
		const val iD = 0x13.toByte()
	}
}