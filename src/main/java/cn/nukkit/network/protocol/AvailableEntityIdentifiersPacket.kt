package cn.nukkit.network.protocol

import cn.nukkit.Nukkit
import com.google.common.io.ByteStreams
import lombok.ToString
import java.io.InputStream
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString(exclude = ["tag"])
class AvailableEntityIdentifiersPacket : DataPacket() {
	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.AVAILABLE_ENTITY_IDENTIFIERS_PACKET
		private val TAG: ByteArray?

		init {
			TAG = try {
				val inputStream: InputStream = Nukkit::class.java.getClassLoader().getResourceAsStream("entity_identifiers.dat")
				if (cn.nukkit.network.protocol.inputStream == null) {
					throw AssertionError("Could not find entity_identifiers.dat")
				}
				ByteStreams.toByteArray(cn.nukkit.network.protocol.inputStream)
			} catch (e: Exception) {
				throw AssertionError("Error whilst loading entity_identifiers.dat", e)
			}
		}
	}

	var tag = TAG

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	@Override
	override fun decode() {
		tag = this.get()
	}

	@Override
	override fun encode() {
		this.reset()
		this.put(tag)
	}
}