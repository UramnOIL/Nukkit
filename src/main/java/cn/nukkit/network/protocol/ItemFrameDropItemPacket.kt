package cn.nukkit.network.protocol

import cn.nukkit.math.BlockVector3
import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * Created by Pub4Game on 03.07.2016.
 */
@ToString
class ItemFrameDropItemPacket : DataPacket() {
	var x = 0
	var y = 0
	var z = 0

	@Override
	override fun decode() {
		val v: BlockVector3 = this.getBlockVector3()
		z = v.z
		y = v.y
		x = v.x
	}

	@Override
	override fun encode() {
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.ITEM_FRAME_DROP_ITEM_PACKET
	}
}