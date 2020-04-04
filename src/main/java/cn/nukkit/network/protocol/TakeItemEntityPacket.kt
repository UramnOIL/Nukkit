package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * Created on 15-10-14.
 */
@ToString
class TakeItemEntityPacket : DataPacket() {
	var entityId: Long = 0
	var target: Long = 0

	@Override
	override fun decode() {
		target = this.getEntityRuntimeId()
		entityId = this.getEntityRuntimeId()
	}

	@Override
	override fun encode() {
		this.reset()
		this.putEntityRuntimeId(target)
		this.putEntityRuntimeId(entityId)
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.TAKE_ITEM_ENTITY_PACKET
	}
}