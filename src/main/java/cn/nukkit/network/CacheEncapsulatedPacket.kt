package cn.nukkit.network

import cn.nukkit.raknet.protocol.EncapsulatedPacket
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class CacheEncapsulatedPacket : EncapsulatedPacket() {
	var internalData: ByteArray? = null

	@Override
	fun toBinary(): ByteArray? {
		return this.toBinary(false)
	}

	@Override
	fun toBinary(internal: Boolean): ByteArray? {
		if (internalData == null) {
			internalData = super.toBinary(internal)
		}
		return internalData
	}
}