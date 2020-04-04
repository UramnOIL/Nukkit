package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class ClientToServerHandshakePacket : DataPacket() {
	@Override
	override fun pid(): Byte {
		return ProtocolInfo.CLIENT_TO_SERVER_HANDSHAKE_PACKET
	}

	@Override
	override fun decode() {
		//no content
	}

	@Override
	override fun encode() {
	}
}