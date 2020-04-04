package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * Created by CreeperFace on 5.3.2017.
 */
@ToString
class MapInfoRequestPacket : DataPacket() {
	var mapId: Long = 0

	@Override
	override fun pid(): Byte {
		return ProtocolInfo.MAP_INFO_REQUEST_PACKET
	}

	@Override
	override fun decode() {
		mapId = this.getEntityUniqueId()
	}

	@Override
	override fun encode() {
	}
}