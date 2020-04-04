package cn.nukkit.network.protocol

import cn.nukkit.math.BlockVector3
import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class NetworkChunkPublisherUpdatePacket : DataPacket() {
	var position: BlockVector3? = null
	var radius = 0

	@Override
	override fun pid(): Byte {
		return ProtocolInfo.NETWORK_CHUNK_PUBLISHER_UPDATE_PACKET
	}

	@Override
	override fun decode() {
		position = this.getSignedBlockPosition()
		radius = this.getUnsignedVarInt() as Int
	}

	@Override
	override fun encode() {
		this.reset()
		this.putSignedBlockPosition(position)
		this.putUnsignedVarInt(radius)
	}
}