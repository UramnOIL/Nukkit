package cn.nukkit.utils

import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.*
import cn.nukkit.math.NukkitMath.round
import cn.nukkit.nbt.NBTIO.read
import cn.nukkit.nbt.NBTIO.write
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class Binary {
	fun unsignShort(value: Int): Int {
		return value and 0xffff
	}

	companion object {
		fun signByte(value: Int): Int {
			return value shl 56 shr 56
		}

		fun unsignByte(value: Int): Int {
			return value and 0xff
		}

		fun signShort(value: Int): Int {
			return value shl 48 shr 48
		}

		fun signInt(value: Int): Int {
			return value shl 32 shr 32
		}

		fun unsignInt(value: Int): Int {
			return value
		}

		//Triad: {0x00,0x00,0x01}<=>1
		fun readTriad(bytes: ByteArray): Int {
			return readInt(byteArrayOf(
					0x00.toByte(),
					bytes[0],
					bytes[1],
					bytes[2]
			))
		}

		fun writeTriad(value: Int): ByteArray {
			return byteArrayOf(
					(value ushr 16 and 0xFF).toByte(),
					(value ushr 8 and 0xFF).toByte(),
					(value and 0xFF).toByte()
			)
		}

		//LTriad: {0x01,0x00,0x00}<=>1
		fun readLTriad(bytes: ByteArray): Int {
			return readLInt(byteArrayOf(
					bytes[0],
					bytes[1],
					bytes[2],
					0x00.toByte()
			))
		}

		fun writeLTriad(value: Int): ByteArray {
			return byteArrayOf(
					(value and 0xFF).toByte(),
					(value ushr 8 and 0xFF).toByte(),
					(value ushr 16 and 0xFF).toByte()
			)
		}

		fun readUUID(bytes: ByteArray): UUID {
			return UUID(readLLong(bytes), readLLong(byteArrayOf(
					bytes[8],
					bytes[9],
					bytes[10],
					bytes[11],
					bytes[12],
					bytes[13],
					bytes[14],
					bytes[15]
			)))
		}

		fun writeUUID(uuid: UUID): ByteArray {
			return appendBytes(writeLLong(uuid.mostSignificantBits), *writeLLong(uuid.leastSignificantBits))
		}

		fun writeMetadata(metadata: EntityMetadata): ByteArray? {
			val stream = BinaryStream()
			val map = metadata.getMap()
			stream.putUnsignedVarInt(map.size.toLong())
			for (id in map.keys) {
				val d = map[id]
				stream.putUnsignedVarInt(id.toLong())
				stream.putUnsignedVarInt(d!!.type.toLong())
				when (d.type) {
					Entity.DATA_TYPE_BYTE -> stream.putByte((d as ByteEntityData?)!!.getData().byteValue())
					Entity.DATA_TYPE_SHORT -> stream.putLShort((d as ShortEntityData?)!!.getData())
					Entity.DATA_TYPE_INT -> stream.putVarInt((d as IntEntityData?)!!.getData())
					Entity.DATA_TYPE_FLOAT -> stream.putLFloat((d as FloatEntityData?)!!.getData())
					Entity.DATA_TYPE_STRING -> {
						val s = (d as StringEntityData?)!!.data
						stream.putUnsignedVarInt(s.toByteArray(StandardCharsets.UTF_8).size.toLong())
						stream.put(s.toByteArray(StandardCharsets.UTF_8))
					}
					Entity.DATA_TYPE_NBT -> {
						val slot = d as NBTEntityData?
						try {
							stream.put(write(slot!!.data, ByteOrder.LITTLE_ENDIAN, true))
						} catch (e: IOException) {
							throw RuntimeException(e)
						}
					}
					Entity.DATA_TYPE_POS -> {
						val pos = d as IntPositionEntityData?
						stream.putVarInt(pos!!.x)
						stream.putVarInt(pos.y)
						stream.putVarInt(pos.z)
					}
					Entity.DATA_TYPE_LONG -> stream.putVarLong((d as LongEntityData?)!!.data)
					Entity.DATA_TYPE_VECTOR3F -> {
						val v3data = d as Vector3fEntityData?
						stream.putLFloat(v3data!!.x)
						stream.putLFloat(v3data.y)
						stream.putLFloat(v3data.z)
					}
				}
			}
			return stream.buffer
		}

		fun readMetadata(payload: ByteArray?): EntityMetadata {
			val stream = BinaryStream()
			stream.buffer = payload
			val count = stream.unsignedVarInt
			val m = EntityMetadata()
			for (i in 0 until count) {
				val key = stream.unsignedVarInt as Int
				val type = stream.unsignedVarInt as Int
				var value: EntityData<*>? = null
				when (type) {
					Entity.DATA_TYPE_BYTE -> value = ByteEntityData(key, stream.byte)
					Entity.DATA_TYPE_SHORT -> value = ShortEntityData(key, stream.lShort)
					Entity.DATA_TYPE_INT -> value = IntEntityData(key, stream.varInt)
					Entity.DATA_TYPE_FLOAT -> value = FloatEntityData(key, stream.lFloat)
					Entity.DATA_TYPE_STRING -> value = StringEntityData(key, stream.string)
					Entity.DATA_TYPE_NBT -> {
						val offset = stream.getOffset()
						val fbais = FastByteArrayInputStream(stream.get())
						value = try {
							val tag = read(fbais, ByteOrder.LITTLE_ENDIAN, true)
							NBTEntityData(key, tag)
						} catch (e: IOException) {
							throw RuntimeException(e)
						}
						stream.setOffset(offset + fbais.position().toInt())
					}
					Entity.DATA_TYPE_POS -> {
						val v3 = stream.signedBlockPosition
						value = IntPositionEntityData(key, v3!!.x, v3.y, v3.z)
					}
					Entity.DATA_TYPE_LONG -> value = LongEntityData(key, stream.varLong)
					Entity.DATA_TYPE_VECTOR3F -> value = Vector3fEntityData(key, stream.vector3f)
				}
				if (value != null) m.put(value)
			}
			return m
		}

		fun readBool(b: Byte): Boolean {
			return b.toInt() == 0
		}

		fun writeBool(b: Boolean): Byte {
			return (if (b) 0x01 else 0x00).toByte()
		}

		fun readSignedByte(b: Byte): Int {
			return b and 0xFF
		}

		fun writeByte(b: Byte): Byte {
			return b
		}

		fun readShort(bytes: ByteArray): Int {
			return (bytes[0] and 0xFF shl 8) + (bytes[1] and 0xFF)
		}

		fun readSignedShort(bytes: ByteArray): Short {
			return readShort(bytes).toShort()
		}

		fun writeShort(s: Int): ByteArray {
			return byteArrayOf(
					(s ushr 8 and 0xFF).toByte(),
					(s and 0xFF).toByte()
			)
		}

		fun readLShort(bytes: ByteArray): Int {
			return (bytes[1] and 0xFF shl 8) + (bytes[0] and 0xFF)
		}

		fun readSignedLShort(bytes: ByteArray): Short {
			return readLShort(bytes).toShort()
		}

		fun writeLShort(s: Int): ByteArray {
			var s = s
			s = s and 0xffff
			return byteArrayOf(
					(s and 0xFF).toByte(),
					(s ushr 8 and 0xFF).toByte()
			)
		}

		fun readInt(bytes: ByteArray): Int {
			return (bytes[0] and 0xff shl 24) +
					(bytes[1] and 0xff shl 16) +
					(bytes[2] and 0xff shl 8) +
					(bytes[3] and 0xff)
		}

		fun writeInt(i: Int): ByteArray {
			return byteArrayOf(
					(i ushr 24 and 0xFF).toByte(),
					(i ushr 16 and 0xFF).toByte(),
					(i ushr 8 and 0xFF).toByte(),
					(i and 0xFF).toByte()
			)
		}

		fun readLInt(bytes: ByteArray): Int {
			return (bytes[3] and 0xff shl 24) +
					(bytes[2] and 0xff shl 16) +
					(bytes[1] and 0xff shl 8) +
					(bytes[0] and 0xff)
		}

		fun writeLInt(i: Int): ByteArray {
			return byteArrayOf(
					(i and 0xFF).toByte(),
					(i ushr 8 and 0xFF).toByte(),
					(i ushr 16 and 0xFF).toByte(),
					(i ushr 24 and 0xFF).toByte()
			)
		}

		@JvmOverloads
		fun readFloat(bytes: ByteArray, accuracy: Int = -1): Float {
			val `val` = java.lang.Float.intBitsToFloat(readInt(bytes))
			return if (accuracy > -1) {
				round(`val`.toDouble(), accuracy).toFloat()
			} else {
				`val`
			}
		}

		fun writeFloat(f: Float): ByteArray {
			return writeInt(java.lang.Float.floatToIntBits(f))
		}

		@JvmOverloads
		fun readLFloat(bytes: ByteArray, accuracy: Int = -1): Float {
			val `val` = java.lang.Float.intBitsToFloat(readLInt(bytes))
			return if (accuracy > -1) {
				round(`val`.toDouble(), accuracy).toFloat()
			} else {
				`val`
			}
		}

		fun writeLFloat(f: Float): ByteArray {
			return writeLInt(java.lang.Float.floatToIntBits(f))
		}

		fun readDouble(bytes: ByteArray): Double {
			return java.lang.Double.longBitsToDouble(readLong(bytes))
		}

		fun writeDouble(d: Double): ByteArray {
			return writeLong(java.lang.Double.doubleToLongBits(d))
		}

		fun readLDouble(bytes: ByteArray): Double {
			return java.lang.Double.longBitsToDouble(readLLong(bytes))
		}

		fun writeLDouble(d: Double): ByteArray {
			return writeLLong(java.lang.Double.doubleToLongBits(d))
		}

		fun readLong(bytes: ByteArray): Long {
			return (bytes[0].toLong() shl 56) +
					((bytes[1] and 0xFF) as Long shl 48) +
					((bytes[2] and 0xFF) as Long shl 40) +
					((bytes[3] and 0xFF) as Long shl 32) +
					((bytes[4] and 0xFF) as Long shl 24) +
					(bytes[5] and 0xFF shl 16) +
					(bytes[6] and 0xFF shl 8) +
					(bytes[7] and 0xFF)
		}

		fun writeLong(l: Long): ByteArray {
			return byteArrayOf(
					(l ushr 56).toByte(),
					(l ushr 48).toByte(),
					(l ushr 40).toByte(),
					(l ushr 32).toByte(),
					(l ushr 24).toByte(),
					(l ushr 16).toByte(),
					(l ushr 8).toByte(),
					l.toByte()
			)
		}

		fun readLLong(bytes: ByteArray): Long {
			return (bytes[7].toLong() shl 56) +
					((bytes[6] and 0xFF) as Long shl 48) +
					((bytes[5] and 0xFF) as Long shl 40) +
					((bytes[4] and 0xFF) as Long shl 32) +
					((bytes[3] and 0xFF) as Long shl 24) +
					(bytes[2] and 0xFF shl 16) +
					(bytes[1] and 0xFF shl 8) +
					(bytes[0] and 0xFF)
		}

		fun writeLLong(l: Long): ByteArray {
			return byteArrayOf(
					l.toByte(),
					(l ushr 8).toByte(),
					(l ushr 16).toByte(),
					(l ushr 24).toByte(),
					(l ushr 32).toByte(),
					(l ushr 40).toByte(),
					(l ushr 48).toByte(),
					(l ushr 56).toByte())
		}

		fun writeVarInt(v: Int): ByteArray? {
			val stream = BinaryStream()
			stream.putVarInt(v)
			return stream.buffer
		}

		fun writeUnsignedVarInt(v: Long): ByteArray? {
			val stream = BinaryStream()
			stream.putUnsignedVarInt(v)
			return stream.buffer
		}

		fun reserveBytes(bytes: ByteArray): ByteArray {
			val newBytes = ByteArray(bytes.size)
			for (i in bytes.indices) {
				newBytes[bytes.size - 1 - i] = bytes[i]
			}
			return newBytes
		}

		@JvmOverloads
		fun bytesToHexString(src: ByteArray?, blank: Boolean = false): String? {
			val stringBuilder = StringBuilder()
			if (src == null || src.size <= 0) {
				return null
			}
			for (b in src) {
				if (stringBuilder.length != 0 && blank) {
					stringBuilder.append(" ")
				}
				val v: Int = b and 0xFF
				val hv = Integer.toHexString(v)
				if (hv.length < 2) {
					stringBuilder.append(0)
				}
				stringBuilder.append(hv)
			}
			return stringBuilder.toString().toUpperCase()
		}

		fun hexStringToBytes(hexString: String?): ByteArray? {
			var hexString = hexString
			if (hexString == null || hexString == "") {
				return null
			}
			val str = "0123456789ABCDEF"
			hexString = hexString.toUpperCase().replace(" ", "")
			val length = hexString.length / 2
			val hexChars = hexString.toCharArray()
			val d = ByteArray(length)
			for (i in 0 until length) {
				val pos = i * 2
				d[i] = (str.indexOf(hexChars[pos]).toByte() shl 4 or str.indexOf(hexChars[pos + 1]).toByte()) as Byte
			}
			return d
		}

		@JvmOverloads
		fun subBytes(bytes: ByteArray, start: Int, length: Int = bytes.size - start): ByteArray {
			val len = Math.min(bytes.size, start + length)
			return Arrays.copyOfRange(bytes, start, len)
		}

		fun splitBytes(bytes: ByteArray, chunkSize: Int): Array<ByteArray> {
			val splits = Array((bytes.size + chunkSize - 1) / chunkSize) { ByteArray(chunkSize) }
			var chunks = 0
			var i = 0
			while (i < bytes.size) {
				if (bytes.size - i > chunkSize) {
					splits[chunks] = Arrays.copyOfRange(bytes, i, i + chunkSize)
				} else {
					splits[chunks] = Arrays.copyOfRange(bytes, i, bytes.size)
				}
				chunks++
				i += chunkSize
			}
			return splits
		}

		fun appendBytes(bytes: Array<ByteArray>): ByteArray {
			var length = 0
			for (b in bytes) {
				length += b.size
			}
			val buffer = ByteBuffer.allocate(length)
			for (b in bytes) {
				buffer.put(b)
			}
			return buffer.array()
		}

		fun appendBytes(byte1: Byte, vararg bytes2: ByteArray): ByteArray {
			var length = 1
			for (bytes in bytes2) {
				length += bytes.size
			}
			val buffer = ByteBuffer.allocate(length)
			buffer.put(byte1)
			for (bytes in bytes2) {
				buffer.put(bytes)
			}
			return buffer.array()
		}

		fun appendBytes(bytes1: ByteArray, vararg bytes2: ByteArray): ByteArray {
			var length = bytes1.size
			for (bytes in bytes2) {
				length += bytes.size
			}
			val buffer = ByteBuffer.allocate(length)
			buffer.put(bytes1)
			for (bytes in bytes2) {
				buffer.put(bytes)
			}
			return buffer.array()
		}
	}
}