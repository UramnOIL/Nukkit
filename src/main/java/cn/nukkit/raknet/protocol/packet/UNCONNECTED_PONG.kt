package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.RakNet
import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class UNCONNECTED_PONG : Packet() {

	var pingID: Long = 0
	var serverID: Long = 0
	var serverName: String? = null
	override fun encode() {
		super.encode()
		putLong(pingID)
		putLong(serverID)
		put(RakNet.MAGIC)
		putString(serverName!!)
	}

	override fun decode() {
		super.decode()
		pingID = this.long
		serverID = this.long
		offset += 16 //skip magic bytes todo:check magic?
		serverName = this.string
	}

	class Factory : PacketFactory {
		override fun create(): Packet {
			return UNCONNECTED_PONG()
		}
	}

	companion object {
		const val iD = 0x1c.toByte()
	}
}