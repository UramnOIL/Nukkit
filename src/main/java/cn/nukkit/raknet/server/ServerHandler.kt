package cn.nukkit.raknet.server

import cn.nukkit.raknet.RakNet
import cn.nukkit.raknet.protocol.EncapsulatedPacket
import cn.nukkit.utils.Binary
import java.nio.charset.StandardCharsets

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ServerHandler(protected val server: RakNetServer, protected val instance: ServerInstance) {
	@JvmOverloads
	fun sendEncapsulated(identifier: String, packet: EncapsulatedPacket, flags: Int = RakNet.PRIORITY_NORMAL.toInt()) {
		val buffer = Binary.appendBytes(
				RakNet.PACKET_ENCAPSULATED, byteArrayOf((identifier.length and 0xff).toByte()),
				identifier.toByteArray(StandardCharsets.UTF_8), byteArrayOf((flags and 0xff).toByte()),
				packet.toBinary(true)
		)
		server.pushMainToThreadPacket(buffer)
	}

	fun sendRaw(address: String, port: Int, payload: ByteArray?) {
		val buffer = Binary.appendBytes(
				RakNet.PACKET_RAW, byteArrayOf((address.length and 0xff).toByte()),
				address.toByteArray(StandardCharsets.UTF_8),
				Binary.writeShort(port),
				payload
		)
		server.pushMainToThreadPacket(buffer)
	}

	fun closeSession(identifier: String, reason: String) {
		val buffer = Binary.appendBytes(
				RakNet.PACKET_CLOSE_SESSION, byteArrayOf((identifier.length and 0xff).toByte()),
				identifier.toByteArray(StandardCharsets.UTF_8), byteArrayOf((reason.length and 0xff).toByte()),
				reason.toByteArray(StandardCharsets.UTF_8)
		)
		server.pushMainToThreadPacket(buffer)
	}

	fun sendOption(name: String, value: String) {
		val buffer = Binary.appendBytes(
				RakNet.PACKET_SET_OPTION, byteArrayOf((name.length and 0xff).toByte()),
				name.toByteArray(StandardCharsets.UTF_8),
				value.toByteArray(StandardCharsets.UTF_8)
		)
		server.pushMainToThreadPacket(buffer)
	}

	fun blockAddress(address: String, timeout: Int) {
		val buffer = Binary.appendBytes(
				RakNet.PACKET_BLOCK_ADDRESS, byteArrayOf((address.length and 0xff).toByte()),
				address.toByteArray(StandardCharsets.UTF_8),
				Binary.writeInt(timeout)
		)
		server.pushMainToThreadPacket(buffer)
	}

	fun unblockAddress(address: String) {
		val buffer = Binary.appendBytes(
				RakNet.PACKET_UNBLOCK_ADDRESS, byteArrayOf((address.length and 0xff).toByte()),
				address.toByteArray(StandardCharsets.UTF_8)
		)
		server.pushMainToThreadPacket(buffer)
	}

	fun shutdown() {
		server.pushMainToThreadPacket(byteArrayOf(RakNet.PACKET_SHUTDOWN))
		server.shutdown()
		synchronized(this) {
			try {
				this.wait(20)
			} catch (e: InterruptedException) {
				//ignore
			}
		}
		try {
			server.join()
		} catch (e: InterruptedException) {
			//ignore
		}
	}

	fun emergencyShutdown() {
		server.shutdown()
		server.pushMainToThreadPacket(byteArrayOf(RakNet.PACKET_EMERGENCY_SHUTDOWN))
	}

	protected fun invalidSession(identifier: String) {
		val buffer = Binary.appendBytes(
				RakNet.PACKET_INVALID_SESSION, byteArrayOf((identifier.length and 0xff).toByte()),
				identifier.toByteArray(StandardCharsets.UTF_8)
		)
		server.pushMainToThreadPacket(buffer)
	}

	fun handlePacket(): Boolean {
		val packet = server.readThreadToMainPacket()
		if (packet != null && packet.size > 0) {
			val id = packet[0]
			var offset = 1
			if (id == RakNet.PACKET_ENCAPSULATED) {
				val len = packet[offset++].toInt()
				val identifier = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
				offset += len
				val flags = packet[offset++].toInt()
				val buffer = Binary.subBytes(packet, offset)
				instance.handleEncapsulated(identifier, EncapsulatedPacket.Companion.fromBinary(buffer, true), flags)
			} else if (id == RakNet.PACKET_RAW) {
				val len = packet[offset++].toInt()
				val address = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
				offset += len
				val port = Binary.readShort(Binary.subBytes(packet, offset, 2)) and 0xffff
				offset += 2
				val payload = Binary.subBytes(packet, offset)
				instance.handleRaw(address, port, payload)
			} else if (id == RakNet.PACKET_SET_OPTION) {
				val len = packet[offset++].toInt()
				val name = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
				offset += len
				val value = String(Binary.subBytes(packet, offset), StandardCharsets.UTF_8)
				instance.handleOption(name, value)
			} else if (id == RakNet.PACKET_OPEN_SESSION) {
				var len = packet[offset++].toInt()
				val identifier = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
				offset += len
				len = packet[offset++].toInt()
				val address = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
				offset += len
				val port = Binary.readShort(Binary.subBytes(packet, offset, 2)) and 0xffff
				offset += 2
				val clientID = Binary.readLong(Binary.subBytes(packet, offset, 8))
				instance.openSession(identifier, address, port, clientID)
			} else if (id == RakNet.PACKET_CLOSE_SESSION) {
				var len = packet[offset++].toInt()
				val identifier = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
				offset += len
				len = packet[offset++].toInt()
				val reason = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
				instance.closeSession(identifier, reason)
			} else if (id == RakNet.PACKET_INVALID_SESSION) {
				val len = packet[offset++].toInt()
				val identifier = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
				instance.closeSession(identifier, "Invalid session")
			} else if (id == RakNet.PACKET_ACK_NOTIFICATION) {
				val len = packet[offset++].toInt()
				val identifier = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
				offset += len
				val identifierACK = Binary.readInt(Binary.subBytes(packet, offset, 4))
				instance.notifyACK(identifier, identifierACK)
			}
			return true
		}
		return false
	}

}