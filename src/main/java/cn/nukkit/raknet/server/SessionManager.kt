package cn.nukkit.raknet.server

import cn.nukkit.raknet.RakNet
import cn.nukkit.raknet.protocol.EncapsulatedPacket
import cn.nukkit.raknet.protocol.Packet
import cn.nukkit.raknet.protocol.Packet.PacketFactory
import cn.nukkit.raknet.protocol.packet.*
import cn.nukkit.utils.Binary
import cn.nukkit.utils.ThreadedLogger
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class SessionManager(protected val server: RakNetServer, protected val socket: UDPServerSocket) {
	protected val packetPool = arrayOfNulls<PacketFactory>(256)
	protected var receiveBytes = 0
	protected var sendBytes = 0
	protected val sessions: MutableMap<String, Session> = HashMap()
	var name = ""
		protected set
	protected var packetLimit = 1000
	protected var shutdown = false
	protected var ticks: Long = 0
	protected var lastMeasure: Long = 0
	protected val block: MutableMap<String, Long> = HashMap()
	protected val ipSec: MutableMap<String, Int> = HashMap()
	var portChecking = true
	val iD: Long
	protected var currentSource = ""
	val port: Int
		get() = server.port

	val logger: ThreadedLogger?
		get() = server.getLogger()

	@Throws(Exception::class)
	fun run() {
		tickProcessor()
	}

	@Throws(Exception::class)
	private fun tickProcessor() {
		lastMeasure = System.currentTimeMillis()
		while (!shutdown) {
			val start = System.currentTimeMillis()
			var max = 5000
			while (max > 0) {
				try {
					if (!receivePacket()) {
						break
					}
					--max
				} catch (e: Exception) {
					if (!currentSource.isEmpty()) {
						blockAddress(currentSource)
					}
					// else ignore
				}
			}
			while (receiveStream());
			val time = System.currentTimeMillis() - start
			if (time < 50) {
				try {
					Thread.sleep(50 - time)
				} catch (e: InterruptedException) {
					//ignore
				}
			}
			tick()
		}
	}

	@Throws(Exception::class)
	private fun tick() {
		val time = System.currentTimeMillis()
		for (session in ArrayList(sessions.values)) {
			session.update(time)
		}
		for (address in ipSec.keys) {
			val count = ipSec[address]!!
			if (count >= packetLimit) {
				blockAddress(address)
			}
		}
		ipSec.clear()
		if (ticks and 15 == 0L) {
			val diff = Math.max(5.0, time.toDouble() - lastMeasure)
			streamOption("bandwidth", (sendBytes / diff).toString() + ";" + receiveBytes / diff)
			lastMeasure = time
			sendBytes = 0
			receiveBytes = 0
			if (!block.isEmpty()) {
				val now = System.currentTimeMillis()
				for (address in ArrayList(block.keys)) {
					val timeout = block[address]!!
					if (timeout <= now) {
						block.remove(address)
						logger!!.notice("Unblocked $address")
					} else {
						break
					}
				}
			}
		}
		++ticks
	}

	@Throws(Exception::class)
	private fun receivePacket(): Boolean {
		val datagramPacket = socket.readPacket()
		if (datagramPacket != null) {
			// Check this early
			try {
				val source = datagramPacket.sender().hostString
				currentSource = source //in order to block address
				if (block.containsKey(source)) {
					return true
				}
				if (ipSec.containsKey(source)) {
					ipSec[source] = ipSec[source]!! + 1
				} else {
					ipSec[source] = 1
				}
				val byteBuf = datagramPacket.content()
				if (byteBuf.readableBytes() == 0) {
					// Exit early to process another packet
					return true
				}
				val buffer = ByteArray(byteBuf.readableBytes())
				byteBuf.readBytes(buffer)
				val len = buffer.size
				val port = datagramPacket.sender().port
				receiveBytes += len
				val pid = buffer[0]
				if (pid == UNCONNECTED_PONG.Companion.ID) {
					return false
				}
				var packet = getPacketFromPool(pid)
				if (packet != null) {
					packet.buffer = buffer
					getSession(source, port)!!.handlePacket(packet)
					return true
				} else if (pid == UNCONNECTED_PING.Companion.ID) {
					packet = UNCONNECTED_PING()
					packet.buffer = buffer
					packet.decode()
					val pk = UNCONNECTED_PONG()
					pk.serverID = iD
					pk.pingID = packet.pingID
					pk.serverName = name
					this.sendPacket(pk, source, port)
				} else if (buffer.size != 0) {
					streamRAW(source, port, buffer)
					return true
				} else {
					return false
				}
			} finally {
				datagramPacket.release()
			}
		}
		return false
	}

	@Throws(IOException::class)
	fun sendPacket(packet: Packet?, dest: String?, port: Int) {
		packet!!.encode()
		sendBytes += socket.writePacket(packet.buffer, dest, port)
	}

	@Throws(IOException::class)
	fun sendPacket(packet: Packet, dest: InetSocketAddress?) {
		packet.encode()
		sendBytes += socket.writePacket(packet.buffer, dest)
	}

	@JvmOverloads
	fun streamEncapsulated(session: Session, packet: EncapsulatedPacket?, flags: Int = RakNet.PRIORITY_NORMAL.toInt()) {
		val id = session.address + ":" + session.port
		val buffer = Binary.appendBytes(
				RakNet.PACKET_ENCAPSULATED, byteArrayOf((id.length and 0xff).toByte()),
				id.toByteArray(StandardCharsets.UTF_8), byteArrayOf((flags and 0xff).toByte()),
				packet!!.toBinary(true)
		)
		server.pushThreadToMainPacket(buffer)
	}

	fun streamRAW(address: String, port: Int, payload: ByteArray?) {
		val buffer = Binary.appendBytes(
				RakNet.PACKET_RAW, byteArrayOf((address.length and 0xff).toByte()),
				address.toByteArray(StandardCharsets.UTF_8),
				Binary.writeShort(port),
				payload
		)
		server.pushThreadToMainPacket(buffer)
	}

	protected fun streamClose(identifier: String, reason: String) {
		val buffer = Binary.appendBytes(
				RakNet.PACKET_CLOSE_SESSION, byteArrayOf((identifier.length and 0xff).toByte()),
				identifier.toByteArray(StandardCharsets.UTF_8), byteArrayOf((reason.length and 0xff).toByte()),
				reason.toByteArray(StandardCharsets.UTF_8)
		)
		server.pushThreadToMainPacket(buffer)
	}

	protected fun streamInvalid(identifier: String) {
		val buffer = Binary.appendBytes(
				RakNet.PACKET_INVALID_SESSION, byteArrayOf((identifier.length and 0xff).toByte()),
				identifier.toByteArray(StandardCharsets.UTF_8)
		)
		server.pushThreadToMainPacket(buffer)
	}

	protected fun streamOpen(session: Session) {
		val identifier = session.address + ":" + session.port
		val buffer = Binary.appendBytes(
				RakNet.PACKET_OPEN_SESSION, byteArrayOf((identifier.length and 0xff).toByte()),
				identifier.toByteArray(StandardCharsets.UTF_8), byteArrayOf((session.address.length and 0xff) as Byte),
				session.address.toByteArray(StandardCharsets.UTF_8),
				Binary.writeShort(session.port),
				Binary.writeLong(session.id)
		)
		server.pushThreadToMainPacket(buffer)
	}

	protected fun streamACK(identifier: String, identifierACK: Int) {
		val buffer = Binary.appendBytes(
				RakNet.PACKET_ACK_NOTIFICATION, byteArrayOf((identifier.length and 0xff).toByte()),
				identifier.toByteArray(StandardCharsets.UTF_8),
				Binary.writeInt(identifierACK)
		)
		server.pushThreadToMainPacket(buffer)
	}

	protected fun streamOption(name: String, value: String) {
		val buffer = Binary.appendBytes(
				RakNet.PACKET_SET_OPTION, byteArrayOf((name.length and 0xff).toByte()),
				name.toByteArray(StandardCharsets.UTF_8),
				value.toByteArray(StandardCharsets.UTF_8)
		)
		server.pushThreadToMainPacket(buffer)
	}

	private fun checkSessions() {
		var size = sessions.size
		if (size > 4096) {
			val keyToRemove: MutableList<String> = ArrayList()
			for (i in sessions.keys) {
				val s = sessions[i]
				if (s!!.isTemporal) {
					keyToRemove.add(i)
					size--
					if (size <= 4096) {
						break
					}
				}
			}
			for (i in keyToRemove) {
				sessions.remove(i)
			}
		}
	}

	@Throws(Exception::class)
	fun receiveStream(): Boolean {
		val packet = server.readMainToThreadPacket()
		if (packet != null && packet.size > 0) {
			val id = packet[0]
			var offset = 1
			when (id) {
				RakNet.PACKET_ENCAPSULATED -> {
					val len = packet[offset++].toInt()
					val identifier = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
					offset += len
					if (sessions.containsKey(identifier)) {
						val flags = packet[offset++]
						val buffer = Binary.subBytes(packet, offset)
						sessions[identifier]!!.addEncapsulatedToQueue(EncapsulatedPacket.Companion.fromBinary(buffer, true), flags.toInt())
					} else {
						streamInvalid(identifier)
					}
				}
				RakNet.PACKET_RAW -> {
					len = packet[offset++]
					val address = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
					offset += len
					val port = Binary.readShort(Binary.subBytes(packet, offset, 2))
					offset += 2
					val payload = Binary.subBytes(packet, offset)
					socket.writePacket(payload, address, port)
				}
				RakNet.PACKET_CLOSE_SESSION -> {
					len = packet[offset++]
					identifier = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
					if (sessions.containsKey(identifier)) {
						removeSession(sessions[identifier])
					} else {
						streamInvalid(identifier)
					}
				}
				RakNet.PACKET_INVALID_SESSION -> {
					len = packet[offset++]
					identifier = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
					if (sessions.containsKey(identifier)) {
						removeSession(sessions[identifier])
					}
				}
				RakNet.PACKET_SET_OPTION -> {
					len = packet[offset++]
					val name = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
					offset += len
					val value = String(Binary.subBytes(packet, offset), StandardCharsets.UTF_8)
					when (name) {
						"name" -> this.name = value
						"portChecking" -> portChecking = java.lang.Boolean.valueOf(value)
						"packetLimit" -> packetLimit = Integer.valueOf(value)
					}
				}
				RakNet.PACKET_BLOCK_ADDRESS -> {
					len = packet[offset++]
					address = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
					offset += len
					val timeout = Binary.readInt(Binary.subBytes(packet, offset, 4))
					blockAddress(address, timeout)
				}
				RakNet.PACKET_UNBLOCK_ADDRESS -> {
					len = packet[offset++]
					address = String(Binary.subBytes(packet, offset, len), StandardCharsets.UTF_8)
					unblockAddress(address)
				}
				RakNet.PACKET_SHUTDOWN -> {
					for (session in ArrayList(sessions.values)) {
						removeSession(session)
					}
					socket.close()
					shutdown = true
				}
				RakNet.PACKET_EMERGENCY_SHUTDOWN -> {
					shutdown = true
					return false
				}
				else -> return false
			}
			return true
		}
		return false
	}

	@JvmOverloads
	fun blockAddress(address: String, timeout: Int = 300) {
		var finalTime = System.currentTimeMillis() + timeout * 1000
		if (!block.containsKey(address) || timeout == -1) {
			if (timeout == -1) {
				finalTime = Long.MAX_VALUE
			} else {
				logger!!.notice("Blocked $address for $timeout seconds")
			}
			block[address] = finalTime
		} else if (block[address]!! < finalTime) {
			block[address] = finalTime
		}
	}

	fun unblockAddress(address: String) {
		block.remove(address)
	}

	fun getSession(ip: String, port: Int): Session? {
		val id = "$ip:$port"
		if (!sessions.containsKey(id)) {
			checkSessions()
			val session = Session(this, ip, port)
			sessions[id] = session
			return session
		}
		return sessions[id]
	}

	@JvmOverloads
	@Throws(Exception::class)
	fun removeSession(session: Session?, reason: String = "unknown") {
		val id = session.getAddress() + ":" + session.getPort()
		if (sessions.containsKey(id)) {
			sessions[id]!!.close()
			sessions.remove(id)
			streamClose(id, reason)
		}
	}

	fun openSession(session: Session) {
		streamOpen(session)
	}

	fun notifyACK(session: Session, identifierACK: Int) {
		streamACK(session.address + ":" + session.port, identifierACK)
	}

	private fun registerPacket(id: Byte, factory: PacketFactory) {
		packetPool[id and 0xFF] = factory
	}

	fun getPacketFromPool(id: Byte): Packet? {
		return packetPool[id and 0xFF]!!.create()
	}

	private fun registerPackets() {
		// fill with dummy returning null
		Arrays.fill(packetPool, PacketFactory { null } as PacketFactory)

		//this.registerPacket(UNCONNECTED_PING.ID, UNCONNECTED_PING.class);
		registerPacket(UNCONNECTED_PING_OPEN_CONNECTIONS.Companion.ID, UNCONNECTED_PING_OPEN_CONNECTIONS.Factory())
		registerPacket(OPEN_CONNECTION_REQUEST_1.Companion.ID, OPEN_CONNECTION_REQUEST_1.Factory())
		registerPacket(OPEN_CONNECTION_REPLY_1.Companion.ID, OPEN_CONNECTION_REPLY_1.Factory())
		registerPacket(OPEN_CONNECTION_REQUEST_2.Companion.ID, OPEN_CONNECTION_REQUEST_2.Factory())
		registerPacket(OPEN_CONNECTION_REPLY_2.Companion.ID, OPEN_CONNECTION_REPLY_2.Factory())
		registerPacket(UNCONNECTED_PONG.Companion.ID, UNCONNECTED_PONG.Factory())
		registerPacket(ADVERTISE_SYSTEM.Companion.ID, ADVERTISE_SYSTEM.Factory())
		registerPacket(DATA_PACKET_0.Companion.ID, DATA_PACKET_0.Factory())
		registerPacket(DATA_PACKET_1.Companion.ID, DATA_PACKET_1.Factory())
		registerPacket(DATA_PACKET_2.Companion.ID, DATA_PACKET_2.Factory())
		registerPacket(DATA_PACKET_3.Companion.ID, DATA_PACKET_3.Factory())
		registerPacket(DATA_PACKET_4.Companion.ID, DATA_PACKET_4.Factory())
		registerPacket(DATA_PACKET_5.Companion.ID, DATA_PACKET_5.Factory())
		registerPacket(DATA_PACKET_6.Companion.ID, DATA_PACKET_6.Factory())
		registerPacket(DATA_PACKET_7.Companion.ID, DATA_PACKET_7.Factory())
		registerPacket(DATA_PACKET_8.Companion.ID, DATA_PACKET_8.Factory())
		registerPacket(DATA_PACKET_9.Companion.ID, DATA_PACKET_9.Factory())
		registerPacket(DATA_PACKET_A.Companion.ID, DATA_PACKET_A.Factory())
		registerPacket(DATA_PACKET_B.Companion.ID, DATA_PACKET_B.Factory())
		registerPacket(DATA_PACKET_C.Companion.ID, DATA_PACKET_C.Factory())
		registerPacket(DATA_PACKET_D.Companion.ID, DATA_PACKET_D.Factory())
		registerPacket(DATA_PACKET_E.Companion.ID, DATA_PACKET_E.Factory())
		registerPacket(DATA_PACKET_F.Companion.ID, DATA_PACKET_F.Factory())
		registerPacket(NACK.Companion.ID, NACK.Factory())
		registerPacket(ACK.Companion.ID, ACK.Factory())
	}

	init {
		registerPackets()
		iD = Random().nextLong()
		this.run()
	}
}