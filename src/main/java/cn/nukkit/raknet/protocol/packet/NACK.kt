package cn.nukkit.raknet.protocol.packet

import cn.nukkit.raknet.protocol.AcknowledgePacket
import cn.nukkit.raknet.protocol.Packet

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class NACK : AcknowledgePacket() {

	class Factory : PacketFactory {
		override fun create(): Packet {
			return NACK()
		}
	}

	companion object {
		const val iD = 0xa0.toByte()
	}
}