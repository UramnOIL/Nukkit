package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class StopSoundPacket : DataPacket() {
	var name: String? = null
	var stopAll = false

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
		this.putString(name)
		this.putBoolean(stopAll)
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.STOP_SOUND_PACKET
	}
}