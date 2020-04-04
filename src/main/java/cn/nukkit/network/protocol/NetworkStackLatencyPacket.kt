package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class NetworkStackLatencyPacket : DataPacket() {
	var timestamp: Long = 0
	var unknownBool = false

	@Override
	override fun pid(): Byte {
		return ProtocolInfo.NETWORK_STACK_LATENCY_PACKET
	}

	@Override
	override fun decode() {
		timestamp = this.getLLong()
	}

	@Override
	override fun encode() {
		this.reset()
		this.putLLong(timestamp)
		this.putBoolean(unknownBool)
	}
}