package cn.nukkit.network.protocol

import cn.nukkit.network.CacheEncapsulatedPacket
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class BatchPacket : DataPacket() {
	var payload: ByteArray?

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	@Override
	override fun decode() {
		payload = this.get()
	}

	@Override
	override fun encode() {
	}

	fun trim() {
		setBuffer(null)
		if (encapsulatedPacket != null) {
			payload = null
			if (encapsulatedPacket is CacheEncapsulatedPacket && !encapsulatedPacket.hasSplit) {
				val cached: CacheEncapsulatedPacket? = encapsulatedPacket
				if (cached!!.internalData != null) cached.buffer = null
			}
		}
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.BATCH_PACKET
	}
}