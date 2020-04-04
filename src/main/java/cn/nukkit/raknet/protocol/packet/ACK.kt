package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.protocol.AcknowledgePacket
import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ACK : AcknowledgePacket() {

	class Factory : PacketFactory {
		override fun create(): Packet {
			return ACK()
		}
	}

	companion object {
		const val iD = 0xc0.toByte()
	}
}