package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.RakNet
import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class OPEN_CONNECTION_REQUEST_2 : Packet() {

	var clientID: Long = 0
	var serverAddress: String? = null
	var serverPort = 0
	var mtuSize: Short = 0
	override fun encode() {
		super.encode()
		put(RakNet.MAGIC)
		this.putAddress(serverAddress!!, serverPort)
		putShort(mtuSize.toInt())
		putLong(clientID)
	}

	override fun decode() {
		super.decode()
		offset += 16 //skip magic bytes
		val address = this.address
		serverAddress = address!!.hostString
		serverPort = address.port
		mtuSize = this.signedShort
		clientID = this.long
	}

	class Factory : PacketFactory {
		override fun create(): Packet {
			return OPEN_CONNECTION_REQUEST_2()
		}
	}

	companion object {
		const val iD = 0x07.toByte()
	}
}