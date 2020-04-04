package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * @author Nukkit Project Team
 */
@ToString
class PlayerInputPacket : DataPacket() {
	var motionX = 0f
	var motionY = 0f
	var jumping = false
	var sneaking = false

	@Override
	override fun decode() {
		motionX = this.getLFloat()
		motionY = this.getLFloat()
		jumping = this.getBoolean()
		sneaking = this.getBoolean()
	}

	@Override
	override fun encode() {
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.PLAYER_INPUT_PACKET
	}
}