package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
class ContainerClosePacket : DataPacket() {
	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	var windowId = 0

	@Override
	override fun decode() {
		windowId = this.getByte() as Byte.toInt()
	}

	@Override
	override fun encode() {
		this.reset()
		this.putByte(windowId.toByte())
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.CONTAINER_CLOSE_PACKET
	}
}