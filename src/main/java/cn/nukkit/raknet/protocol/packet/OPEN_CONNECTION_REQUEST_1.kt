package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.RakNet
import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class OPEN_CONNECTION_REQUEST_1 : Packet() {

	var protocol = RakNet.PROTOCOL
	var mtuSize: Short = 0
	override fun encode() {
		super.encode()
		put(RakNet.MAGIC)
		putByte(protocol)
		put(ByteArray(mtuSize - 18))
	}

	override fun decode() {
		super.decode()
		offset += 16 //skip magic bytes
		protocol = this.byte
		mtuSize = buffer!!.size.toShort()
	}

	class Factory : PacketFactory {
		override fun create(): Packet {
			return OPEN_CONNECTION_REQUEST_1()
		}
	}

	companion object {
		const val iD = 0x05.toByte()
	}
}