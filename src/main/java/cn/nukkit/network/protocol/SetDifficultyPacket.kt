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
class SetDifficultyPacket : DataPacket() {
	var difficulty = 0

	@Override
	override fun decode() {
		difficulty = this.getUnsignedVarInt() as Int
	}

	@Override
	override fun encode() {
		this.reset()
		this.putUnsignedVarInt(difficulty)
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.SET_DIFFICULTY_PACKET
	}
}