package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * Created on 15-10-22.
 */
@ToString
class SetEntityLinkPacket : DataPacket() {
	var vehicleUniqueId //from
			: Long = 0
	var riderUniqueId //to
			: Long = 0
	var type: Byte = 0
	var immediate: Byte = 0

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		this.putEntityUniqueId(vehicleUniqueId)
		this.putEntityUniqueId(riderUniqueId)
		this.putByte(type)
		this.putByte(immediate)
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.SET_ENTITY_LINK_PACKET
		const val TYPE_REMOVE: Byte = 0
		const val TYPE_RIDE: Byte = 1
		const val TYPE_PASSENGER: Byte = 2
	}
}