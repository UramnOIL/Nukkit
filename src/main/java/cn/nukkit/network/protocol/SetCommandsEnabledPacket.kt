package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class SetCommandsEnabledPacket : DataPacket() {
	var enabled = false

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
		this.putBoolean(enabled)
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.SET_COMMANDS_ENABLED_PACKET
	}
}