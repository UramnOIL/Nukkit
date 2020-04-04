package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class AddBehaviorTreePacket : DataPacket() {
	var unknown: String? = null

	@Override
	override fun pid(): Byte {
		return ProtocolInfo.ADD_BEHAVIOR_TREE_PACKET
	}

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		this.putString(unknown)
	}
}