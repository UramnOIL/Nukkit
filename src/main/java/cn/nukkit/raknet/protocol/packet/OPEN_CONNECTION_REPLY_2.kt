package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.RakNet
import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class OPEN_CONNECTION_REPLY_2 : Packet() {

	var serverID: Long = 0
	var clientAddress: String? = null
	var clientPort = 0
	var mtuSize: Short = 0
	override fun encode() {
		super.encode()
		put(RakNet.MAGIC)
		putLong(serverID)
		this.putAddress(clientAddress!!, clientPort)
		putShort(mtuSize.toInt())
		putByte(0.toByte()) //server security
	}

	override fun decode() {
		super.decode()
		offset += 16 //skip magic bytes
		serverID = this.long
		val address = this.address
		clientAddress = address!!.hostString
		clientPort = address.port
		mtuSize = this.signedShort
	}

	class Factory : PacketFactory {
		override fun create(): Packet {
			return OPEN_CONNECTION_REPLY_2()
		}
	}

	companion object {
		const val iD = 0x08.toByte()
	}
}