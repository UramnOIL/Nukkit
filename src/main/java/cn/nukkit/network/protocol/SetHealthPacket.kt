package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class SetHealthPacket : DataPacket() {
	var health = 0

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		this.putUnsignedVarInt(health)
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.SET_HEALTH_PACKET
	}
}