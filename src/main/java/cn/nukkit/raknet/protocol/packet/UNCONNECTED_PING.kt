package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.RakNet
import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class UNCONNECTED_PING : Packet() {

	var pingID: Long = 0
	override fun encode() {
		super.encode()
		putLong(pingID)
		put(RakNet.MAGIC)
	}

	override fun decode() {
		super.decode()
		pingID = this.long
	}

	class Factory : PacketFactory {
		override fun create(): Packet {
			return UNCONNECTED_PING()
		}
	}

	companion object {
		const val iD = 0x01.toByte()
	}
}