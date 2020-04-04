package cn.nukkit.network.protocol

import cn.nukkit.Server
import cn.nukkit.raknet.protocol.EncapsulatedPacket
import cn.nukkit.utils.Binary
import cn.nukkit.utils.BinaryStream
import cn.nukkit.utils.Zlib
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class DataPacket : BinaryStream(), Cloneable {
	var isEncoded = false
	var channel = 0
	var encapsulatedPacket: EncapsulatedPacket? = null
	var reliability: Byte = 0
	var orderIndex: Integer? = null
	var orderChannel: Integer? = null
	abstract fun pid(): Byte
	abstract fun decode()
	abstract fun encode()

	@Override
	fun reset(): DataPacket? {
		super.reset()
		this.putUnsignedVarInt(pid() and 0xff)
		return this
	}

	fun clean(): DataPacket? {
		this.setBuffer(null)
		this.setOffset(0)
		isEncoded = false
		return this
	}

	@Override
	fun clone(): DataPacket? {
		return try {
			super.clone() as DataPacket?
		} catch (e: CloneNotSupportedException) {
			null
		}
	}

	fun compress(): BatchPacket? {
		return compress(Server.instance.networkCompressionLevel)
	}

	fun compress(level: Int): BatchPacket? {
		val batch = BatchPacket()
		val batchPayload = arrayOfNulls<ByteArray?>(2)
		val buf: ByteArray = getBuffer()
		batchPayload[0] = Binary.writeUnsignedVarInt(buf.size)
		batchPayload[1] = buf
		val data: ByteArray = Binary.appendBytes(batchPayload)
		try {
			batch!!.payload = Zlib.deflate(data, level)
		} catch (e: Exception) {
			throw RuntimeException(e)
		}
		return batch
	}
}