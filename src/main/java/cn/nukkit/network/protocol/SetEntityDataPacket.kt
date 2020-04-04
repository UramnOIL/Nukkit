package cn.nukkit.network.protocol

import cn.nukkit.entity.data.EntityMetadata
import cn.nukkit.utils.Binary
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
class SetEntityDataPacket : DataPacket() {
	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	var eid: Long = 0
	var metadata: EntityMetadata? = null

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		this.putEntityRuntimeId(eid)
		this.put(Binary.writeMetadata(metadata))
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.SET_ENTITY_DATA_PACKET
	}
}