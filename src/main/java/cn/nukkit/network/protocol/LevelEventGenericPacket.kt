package cn.nukkit.network.protocol

import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import java.io.IOException
import java.nio.ByteOrder
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

class LevelEventGenericPacket : DataPacket() {
	var eventId = 0
	var tag: CompoundTag? = null

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
		this.putVarInt(eventId)
		try {
			this.put(NBTIO.write(tag, ByteOrder.LITTLE_ENDIAN, true))
		} catch (e: IOException) {
			throw RuntimeException(e)
		}
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.LEVEL_EVENT_GENERIC_PACKET
	}
}