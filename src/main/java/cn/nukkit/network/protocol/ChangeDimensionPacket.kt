package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * Created on 2016/1/5 by xtypr.
 * Package cn.nukkit.network.protocol in project nukkit .
 */
@ToString
class ChangeDimensionPacket : DataPacket() {
	var dimension = 0
	var x = 0f
	var y = 0f
	var z = 0f
	var respawn = false

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		this.putVarInt(dimension)
		this.putVector3f(x, y, z)
		this.putBoolean(respawn)
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.CHANGE_DIMENSION_PACKET
	}
}