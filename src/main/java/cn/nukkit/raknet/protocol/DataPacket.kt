package cn.nukkit.raknet.protocol

import cn.nukkit.utils.Binary
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class DataPacket : Packet() {
	var packets = ConcurrentLinkedQueue<Any?>()
	var seqNumber: Int? = null
	override fun encode() {
		super.encode()
		putLTriad(seqNumber!!)
		for (packet in packets) {
			put((if (packet is EncapsulatedPacket) packet.toBinary() else packet as ByteArray?)!!)
		}
	}

	fun length(): Int {
		var length = 4
		for (packet in packets) {
			length += if (packet is EncapsulatedPacket) packet.totalLength else (packet as ByteArray?)!!.size
		}
		return length
	}

	override fun decode() {
		super.decode()
		seqNumber = this.lTriad
		while (!feof()) {
			val data = Binary.subBytes(buffer, offset)
			val packet: EncapsulatedPacket = EncapsulatedPacket.Companion.fromBinary(data, false)
			offset += packet.offset
			if (packet.buffer.size == 0) {
				break
			}
			packets.add(packet)
		}
	}

	override fun clean(): Packet? {
		packets = ConcurrentLinkedQueue()
		seqNumber = null
		return super.clean()
	}

	@Throws(CloneNotSupportedException::class)
	override fun clone(): DataPacket {
		val packet = super.clone() as DataPacket
		packet.packets = ConcurrentLinkedQueue(packets)
		return packet
	}
}