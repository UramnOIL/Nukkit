package cn.nukkit.raknet.protocol

import cn.nukkit.utils.Binary
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class Packet : Cloneable {
	protected var offset = 0
	var buffer: ByteArray?
	var sendTime: Long? = null
	abstract val iD: Byte
	protected operator fun get(len: Int): ByteArray {
		if (len < 0) {
			offset = buffer!!.size - 1
			return ByteArray(0)
		}
		val buffer = ByteArray(len)
		for (i in 0 until len) {
			buffer[i] = this.buffer!![offset++]
		}
		return buffer
	}

	protected val all: ByteArray
		protected get() = this.get()

	protected fun get(): ByteArray {
		return try {
			Arrays.copyOfRange(buffer, offset, buffer!!.size - 1)
		} catch (e: Exception) {
			ByteArray(0)
		}
	}

	protected val long: Long
		protected get() = Binary.readLong(this[8])

	protected val int: Int
		protected get() = Binary.readInt(this[4])

	protected val signedShort: Short
		protected get() = short.toShort()

	protected val short: Int
		protected get() = Binary.readShort(this[2])

	protected val triad: Int
		protected get() = Binary.readTriad(this[3])

	protected val lTriad: Int
		protected get() = Binary.readLTriad(this[3])

	protected val byte: Byte
		protected get() = buffer!![offset++]

	protected val string: String
		protected get() = String(this[signedShort.toInt()], StandardCharsets.UTF_8)

	//todo IPV6 SUPPORT
	protected val address: InetSocketAddress?
		protected get() {
			val version = byte
			return if (version.toInt() == 4) {
				val addr: String = (byte.inv() and 0xff).toString() + "." + (byte.inv() and 0xff) + "." + (byte.inv() and 0xff) + "." + (byte.inv() and 0xff)
				val port = short
				InetSocketAddress(addr, port)
			} else {
				//todo IPV6 SUPPORT
				null
			}
		}

	protected fun feof(): Boolean {
		return !(offset >= 0 && offset + 1 <= buffer!!.size)
	}

	protected fun put(b: ByteArray) {
		buffer = Binary.appendBytes(buffer, *b)
	}

	protected fun putLong(v: Long) {
		put(Binary.writeLong(v))
	}

	protected fun putInt(v: Int) {
		put(Binary.writeInt(v))
	}

	protected fun putShort(v: Int) {
		put(Binary.writeShort(v))
	}

	protected fun putSignedShort(v: Short) {
		put(Binary.writeShort(v and 0xffff))
	}

	protected fun putTriad(v: Int) {
		put(Binary.writeTriad(v))
	}

	protected fun putLTriad(v: Int) {
		put(Binary.writeLTriad(v))
	}

	protected fun putByte(b: Byte) {
		val newBytes = ByteArray(buffer!!.size + 1)
		System.arraycopy(buffer, 0, newBytes, 0, buffer!!.size)
		newBytes[buffer!!.size] = b
		buffer = newBytes
	}

	protected fun putString(str: String) {
		val b = str.toByteArray(StandardCharsets.UTF_8)
		putShort(b.size)
		put(b)
	}

	protected fun putAddress(addr: String, port: Int, version: Byte = 4.toByte()) {
		putByte(version)
		if (version.toInt() == 4) {
			for (b in addr.split("\\.").toTypedArray()) {
				putByte((Integer.valueOf(b).inv() and 0xff).toByte())
			}
			putShort(port)
		} else {
			//todo ipv6
		}
	}

	protected fun putAddress(address: InetSocketAddress) {
		this.putAddress(address.hostString, address.port)
	}

	open fun encode() {
		buffer = byteArrayOf(iD)
	}

	open fun decode() {
		offset = 1
	}

	open fun clean(): Packet? {
		buffer = null
		offset = 0
		sendTime = null
		return this
	}

	@Throws(CloneNotSupportedException::class)
	public override fun clone(): Packet {
		val packet = super.clone() as Packet
		packet.buffer = buffer!!.clone()
		return packet
	}

	/**
	 * A factory to create new packet instances
	 */
	interface PacketFactory {
		/**
		 * Creates the packet
		 */
		fun create(): Packet
	}
}