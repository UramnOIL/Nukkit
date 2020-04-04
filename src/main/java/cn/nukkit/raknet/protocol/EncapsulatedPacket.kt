package cn.nukkit.raknet.protocol

import cn.nukkit.utils.Binary
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class EncapsulatedPacket : Cloneable {
	var reliability = 0
	var hasSplit = false
	var length = 0
	var messageIndex: Int? = null
	var orderIndex: Int? = null
	var orderChannel: Int? = null
	var splitCount: Int? = null
	var splitID: Int? = null
	var splitIndex: Int? = null
	var buffer: ByteArray
	var needACK = false
	var identifierACK: Int? = null
	var offset = 0
		private set

	val totalLength: Int
		get() = 3 + buffer.size + (if (messageIndex != null) 3 else 0) + (if (orderIndex != null) 4 else 0) + if (hasSplit) 10 else 0

	open fun toBinary(): ByteArray? {
		return toBinary(false)
	}

	open fun toBinary(internal: Boolean): ByteArray? {
		val stream = ByteArrayOutputStream()
		try {
			stream.write(reliability shl 5 or if (hasSplit) 16 else 0)
			if (internal) {
				stream.write(Binary.writeInt(buffer.size))
				stream.write(Binary.writeInt((if (identifierACK == null) 0 else identifierACK)!!))
			} else {
				stream.write(Binary.writeShort(buffer.size shl 3))
			}
			if (reliability > 0) {
				if (reliability >= 2 && reliability != 5) {
					stream.write(Binary.writeLTriad((if (messageIndex == null) 0 else messageIndex)!!))
				}
				if (reliability <= 4 && reliability != 2) {
					stream.write(Binary.writeLTriad(orderIndex!!))
					stream.write((orderChannel!! and 0xff) as Byte.toInt())
				}
			}
			if (hasSplit) {
				stream.write(Binary.writeInt(splitCount!!))
				stream.write(Binary.writeShort(splitID!!))
				stream.write(Binary.writeInt(splitIndex!!))
			}
			stream.write(buffer)
		} catch (e: IOException) {
			throw RuntimeException(e)
		}
		return stream.toByteArray()
	}

	override fun toString(): String {
		return Binary.bytesToHexString(this.toBinary())
	}

	@Throws(CloneNotSupportedException::class)
	public override fun clone(): EncapsulatedPacket {
		val packet = super.clone() as EncapsulatedPacket
		packet.buffer = buffer.clone()
		return packet
	}

	companion object {
		@JvmOverloads
		fun fromBinary(binary: ByteArray, internal: Boolean = false): EncapsulatedPacket {
			val packet = EncapsulatedPacket()
			val flags: Int = binary[0] and 0xff
			packet.reliability = flags and 224 shr 5
			packet.hasSplit = flags and 16 > 0
			val length: Int
			var offset: Int
			if (internal) {
				length = Binary.readInt(Binary.subBytes(binary, 1, 4))
				packet.identifierACK = Binary.readInt(Binary.subBytes(binary, 5, 4))
				offset = 9
			} else {
				length = Math.ceil(Binary.readShort(Binary.subBytes(binary, 1, 2)).toDouble() / 8).toInt()
				offset = 3
				packet.identifierACK = null
			}
			if (packet.reliability > 0) {
				if (packet.reliability >= 2 && packet.reliability != 5) {
					packet.messageIndex = Binary.readLTriad(Binary.subBytes(binary, offset, 3))
					offset += 3
				}
				if (packet.reliability <= 4 && packet.reliability != 2) {
					packet.orderIndex = Binary.readLTriad(Binary.subBytes(binary, offset, 3))
					offset += 3
					packet.orderChannel = binary[offset++] and 0xff
				}
			}
			if (packet.hasSplit) {
				packet.splitCount = Binary.readInt(Binary.subBytes(binary, offset, 4))
				offset += 4
				packet.splitID = Binary.readShort(Binary.subBytes(binary, offset, 2))
				offset += 2
				packet.splitIndex = Binary.readInt(Binary.subBytes(binary, offset, 4))
				offset += 4
			}
			packet.buffer = Binary.subBytes(binary, offset, length)
			offset += length
			packet.offset = offset
			return packet
		}
	}
}