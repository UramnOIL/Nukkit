package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class SimpleEventPacket : DataPacket() {
	var unknown: Short = 0

	@Override
	override fun pid(): Byte {
		return ProtocolInfo.SIMPLE_EVENT_PACKET
	}

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		this.putShort(unknown)
	}
}