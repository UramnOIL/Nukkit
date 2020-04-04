package cn.nukkit.network.protocol

import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

class ClientCacheStatusPacket : DataPacket() {
	var supported = false

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	@Override
	override fun decode() {
		supported = this.getBoolean()
	}

	@Override
	override fun encode() {
		this.reset()
		this.putBoolean(supported)
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.CLIENT_CACHE_STATUS_PACKET
	}
}