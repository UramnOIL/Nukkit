package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.RakNet
import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class OPEN_CONNECTION_REPLY_1 : Packet() {

	var serverID: Long = 0
	var mtuSize: Short = 0
	override fun encode() {
		super.encode()
		put(RakNet.MAGIC)
		putLong(serverID)
		putByte(0.toByte()) //server security
		putShort(mtuSize.toInt())
	}

	override fun decode() {
		super.decode()
		offset += 16 //skip magic bytes
		serverID = this.long
		this.byte //skip security
		mtuSize = this.signedShort
	}

	class Factory : PacketFactory {
		override fun create(): Packet {
			return OPEN_CONNECTION_REPLY_1()
		}
	}

	companion object {
		const val iD = 0x06.toByte()
	}
}