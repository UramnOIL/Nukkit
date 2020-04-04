package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class SetLastHurtByPacket : DataPacket() {
	@Override
	override fun pid(): Byte {
		return ProtocolInfo.SET_LAST_HURT_BY_PACKET
	}

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		//TODO
	}
}