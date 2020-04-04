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
class HurtArmorPacket : DataPacket() {
	var health = 0

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		this.putVarInt(health)
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.HURT_ARMOR_PACKET
	}
}