package cn.nukkit.network.protocol

import lombok.ToString
import java.util.UUID
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString(exclude = "data")
class ResourcePackChunkDataPacket : DataPacket() {
	var packId: UUID? = null
	var chunkIndex = 0
	var progress: Long = 0
	var data: ByteArray?

	@Override
	override fun decode() {
		packId = UUID.fromString(this.getString())
		chunkIndex = this.getLInt()
		progress = this.getLLong()
		data = this.getByteArray()
	}

	@Override
	override fun encode() {
		this.reset()
		this.putString(packId.toString())
		this.putLInt(chunkIndex)
		this.putLLong(progress)
		this.putByteArray(data)
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.RESOURCE_PACK_CHUNK_DATA_PACKET
	}
}