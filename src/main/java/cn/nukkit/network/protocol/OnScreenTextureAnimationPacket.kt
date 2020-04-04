package cn.nukkit.network.protocol

import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

class OnScreenTextureAnimationPacket : DataPacket() {
	var effectId = 0

	@Override
	override fun pid(): Byte {
		return ProtocolInfo.ON_SCREEN_TEXTURE_ANIMATION_PACKET
	}

	@Override
	override fun decode() {
		effectId = this.getLInt()
	}

	@Override
	override fun encode() {
		this.reset()
		this.putLInt(effectId)
	}
}