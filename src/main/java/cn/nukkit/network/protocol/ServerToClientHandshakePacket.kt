package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class ServerToClientHandshakePacket : DataPacket() {
	@Override
	override fun pid(): Byte {
		return ProtocolInfo.SERVER_TO_CLIENT_HANDSHAKE_PACKET
	}

	var publicKey: String? = null
	var serverToken: String? = null
	var privateKey: String? = null

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		//TODO
	}
}