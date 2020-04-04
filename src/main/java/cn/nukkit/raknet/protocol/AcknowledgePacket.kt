package cn.nukkit.raknet.protocol

import cn.nukkit.utils.Binary
import cn.nukkit.utils.BinaryStream
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class AcknowledgePacket : Packet() {
	var packets: TreeMap<Int, Int>? = null
	override fun encode() {
		super.encode()
		val count = packets!!.size
		val packets = IntArray(count)
		var index = 0
		for (i in this.packets!!.values) {
			packets[index++] = i
		}
		var records: Short = 0
		val payload = BinaryStream()
		if (count > 0) {
			var pointer = 1
			var start = packets[0]
			var last = packets[0]
			while (pointer < count) {
				val current = packets[pointer++]
				val diff = current - last
				if (diff == 1) {
					last = current
				} else if (diff > 1) {
					if (start == last) {
						payload.putByte(0x01.toByte())
						payload.put(Binary.writeLTriad(start))
						last = current
						start = last
					} else {
						payload.putByte(0x00.toByte())
						payload.put(Binary.writeLTriad(start))
						payload.put(Binary.writeLTriad(last))
						last = current
						start = last
					}
					++records
				}
			}
			if (start == last) {
				payload.putByte(0x01.toByte())
				payload.put(Binary.writeLTriad(start))
			} else {
				payload.putByte(0x00.toByte())
				payload.put(Binary.writeLTriad(start))
				payload.put(Binary.writeLTriad(last))
			}
			++records
		}
		putShort(records.toInt())
		buffer = Binary.appendBytes(
				buffer,
				*payload.buffer
		)
	}

	override fun decode() {
		super.decode()
		val count = this.signedShort
		packets = TreeMap()
		var cnt = 0
		var i = 0
		while (i < count && !feof() && cnt < 4096) {
			if (this.byte.toInt() == 0) {
				val start = this.lTriad
				var end = this.lTriad
				if (end - start > 512) {
					end = start + 512
				}
				for (c in start..end) {
					packets!![cnt++] = c
				}
			} else {
				packets!![cnt++] = this.lTriad
			}
			++i
		}
	}

	override fun clean(): Packet? {
		packets = TreeMap()
		return super.clean()
	}

	@Throws(CloneNotSupportedException::class)
	override fun clone(): AcknowledgePacket {
		val packet = super.clone() as AcknowledgePacket
		packet.packets = TreeMap(packets)
		return packet
	}
}