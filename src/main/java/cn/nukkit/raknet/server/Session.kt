package cn.nukkit.raknet.server

import cn.nukkit.math.NukkitMath.clamp
import cn.nukkit.raknet.RakNet
import cn.nukkit.raknet.protocol.DataPacket
import cn.nukkit.raknet.protocol.EncapsulatedPacket
import cn.nukkit.raknet.protocol.Packet
import cn.nukkit.raknet.protocol.packet.*
import cn.nukkit.utils.Binary
import cn.nukkit.utils.BinaryStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class Session(private var sessionManager: SessionManager?, val address: String, val port: Int) {
	private var messageIndex = 0
	private val channelIndex: MutableMap<Int?, Int> = ConcurrentHashMap()
	var state = STATE_UNCONNECTED
		private set

	//private List<EncapsulatedPacket> preJoinQueue = new ArrayList<>();
	private var mtuSize = MIN_MTU_SIZE
	var iD: Long = 0
		private set
	private var splitID = 0
	private var sendSeqNumber = 0
	private var lastSeqNumber = -1
	private var lastUpdate: Long
	private val startTime: Long
	var isTemporal = true
		private set
	private val packetToSend: MutableList<DataPacket?> = ArrayList()
	private var isActive: Boolean
	private var ACKQueue: MutableMap<Int?, Int?> = HashMap()
	private var NACKQueue: MutableMap<Int?, Int?> = HashMap()
	private val recoveryQueue: MutableMap<Int?, DataPacket?> = TreeMap()
	private val splitPackets: MutableMap<Int?, MutableMap<Int?, EncapsulatedPacket?>> = HashMap()
	private val needACK: MutableMap<Int?, MutableMap<Int?, Int?>> = TreeMap()
	private var sendQueue: DataPacket
	private var windowStart: Int
	private val receivedWindow: MutableMap<Int?, Int?> = TreeMap()
	private var windowEnd: Int
	private var reliableWindowStart: Int
	private var reliableWindowEnd: Int
	private val reliableWindow: MutableMap<Int?, EncapsulatedPacket> = TreeMap()
	private var lastReliableIndex = -1

	@Throws(Exception::class)
	fun update(time: Long) {
		if (!isActive && lastUpdate + 10000 < time) { //10 second timeout
			disconnect("timeout")
			return
		}
		isActive = false
		if (!ACKQueue.isEmpty()) {
			val pk = ACK()
			pk.packets = TreeMap(ACKQueue)
			sendPacket(pk)
			ACKQueue = HashMap()
		}
		if (!NACKQueue.isEmpty()) {
			val pk = NACK()
			pk.packets = TreeMap(NACKQueue)
			sendPacket(pk)
			NACKQueue = HashMap()
		}
		if (!packetToSend.isEmpty()) {
			var limit = 16
			for (i in packetToSend.indices) {
				val pk = packetToSend[i]
				pk!!.sendTime = time
				pk.encode()
				recoveryQueue[pk.seqNumber] = pk
				packetToSend.remove(pk)
				sendPacket(pk)
				if (limit-- <= 0) {
					break
				}
			}
		}
		if (packetToSend.size > WINDOW_SIZE) {
			packetToSend.clear()
		}
		if (!needACK.isEmpty()) {
			for (identifierACK in ArrayList(needACK.keys)) {
				val indexes: Map<Int?, Int?> = needACK[identifierACK]!!
				if (indexes.isEmpty()) {
					needACK.remove(identifierACK)
					sessionManager!!.notifyACK(this, identifierACK)
				}
			}
		}
		for (seq in ArrayList(recoveryQueue.keys)) {
			val pk = recoveryQueue[seq]
			if (pk!!.sendTime!! < System.currentTimeMillis() - 8000) {
				packetToSend.add(pk)
				recoveryQueue.remove(seq)
			} else {
				break
			}
		}
		for (seq in ArrayList(receivedWindow.keys)) {
			if (seq < windowStart) {
				receivedWindow.remove(seq)
			} else {
				break
			}
		}
		sendQueue()
	}

	@JvmOverloads
	@Throws(Exception::class)
	fun disconnect(reason: String = "unknown") {
		sessionManager!!.removeSession(this, reason)
	}

	@Throws(IOException::class)
	private fun sendPacket(packet: Packet?) {
		sessionManager!!.sendPacket(packet, address, port)
	}

	@Throws(IOException::class)
	fun sendQueue() {
		if (!sendQueue.packets.isEmpty()) {
			sendQueue.seqNumber = sendSeqNumber++
			sendPacket(sendQueue)
			sendQueue.sendTime = System.currentTimeMillis()
			recoveryQueue[sendQueue.seqNumber] = sendQueue
			sendQueue = DATA_PACKET_4()
		}
	}

	@Throws(Exception::class)
	private fun addToQueue(pk: EncapsulatedPacket, flags: Int = RakNet.PRIORITY_NORMAL.toInt()) {
		val priority = flags and 7
		if (pk.needACK && pk.messageIndex != null) {
			if (!needACK.containsKey(pk.identifierACK)) {
				needACK[pk.identifierACK] = HashMap()
			}
			needACK[pk.identifierACK]!![pk.messageIndex] = pk.messageIndex
		}
		if (priority == RakNet.PRIORITY_IMMEDIATE.toInt()) { //Skip queues
			val packet: DataPacket = DATA_PACKET_0()
			packet.seqNumber = sendSeqNumber++
			if (pk.needACK) {
				packet.packets.add(pk.clone())
				pk.needACK = false
			} else {
				packet.packets.add(pk.toBinary())
			}
			sendPacket(packet)
			packet.sendTime = System.currentTimeMillis()
			recoveryQueue[packet.seqNumber] = packet
			return
		}
		val length = sendQueue.length()
		if (length + pk.totalLength > mtuSize) {
			sendQueue()
		}
		if (pk.needACK) {
			sendQueue.packets.add(pk.clone())
			pk.needACK = false
		} else {
			sendQueue.packets.add(pk.toBinary())
		}
	}

	@JvmOverloads
	@Throws(Exception::class)
	fun addEncapsulatedToQueue(packet: EncapsulatedPacket, flags: Int = RakNet.PRIORITY_NORMAL.toInt()) {
		if (flags and RakNet.FLAG_NEED_ACK.toInt() > 0.also { packet.needACK = it }) {
			needACK[packet.identifierACK] = HashMap()
		}
		if (packet.reliability == 2 || packet.reliability == 3 || packet.reliability == 4 || packet.reliability == 6 || packet.reliability == 7) {
			packet.messageIndex = messageIndex++
			if (packet.reliability == 3) {
				val index = channelIndex[packet.orderChannel]!! + 1
				packet.orderIndex = index
				channelIndex[packet.orderChannel] = index
			}
		}
		if (packet.totalLength + 4 > mtuSize) {
			val buffers = Binary.splitBytes(packet.buffer, mtuSize - 34)
			val splitID = ++splitID % 65536
			for (count in buffers.indices) {
				val buffer = buffers[count]
				val pk = EncapsulatedPacket()
				pk.splitID = splitID
				pk.hasSplit = true
				pk.splitCount = buffers.size
				pk.reliability = packet.reliability
				pk.splitIndex = count
				pk.buffer = buffer
				if (count > 0) {
					pk.messageIndex = messageIndex++
				} else {
					pk.messageIndex = packet.messageIndex
				}
				if (pk.reliability == 3) {
					pk.orderChannel = packet.orderChannel
					pk.orderIndex = packet.orderIndex
				}
				addToQueue(pk, flags or RakNet.PRIORITY_IMMEDIATE.toInt())
			}
		} else {
			addToQueue(packet, flags)
		}
	}

	@Throws(Exception::class)
	private fun handleSplit(packet: EncapsulatedPacket?) {
		if (packet!!.splitCount!! >= MAX_SPLIT_SIZE || packet.splitIndex!! >= MAX_SPLIT_SIZE || packet.splitIndex!! < 0) {
			return
		}
		if (!splitPackets.containsKey(packet.splitID)) {
			if (splitPackets.size >= MAX_SPLIT_COUNT) {
				return
			}
			splitPackets[packet.splitID] = object : HashMap<Int?, EncapsulatedPacket?>() {
				init {
					put(packet.splitIndex, packet)
				}
			}
		} else {
			splitPackets[packet.splitID]!![packet.splitIndex] = packet
		}
		if (splitPackets[packet.splitID]!!.size == packet.splitCount) {
			val pk = EncapsulatedPacket()
			val stream = BinaryStream()
			for (i in 0 until packet.splitCount!!) {
				stream.put(splitPackets[packet.splitID]!![i]!!.buffer)
			}
			pk.buffer = stream.buffer
			pk.length = pk.buffer.size
			splitPackets.remove(packet.splitID)
			handleEncapsulatedPacketRoute(pk)
		}
	}

	@Throws(Exception::class)
	private fun handleEncapsulatedPacket(packet: EncapsulatedPacket) {
		if (packet.messageIndex == null) {
			handleEncapsulatedPacketRoute(packet)
		} else {
			if (packet.messageIndex!! < reliableWindowStart || packet.messageIndex!! > reliableWindowEnd) {
				return
			}
			if (packet.messageIndex!! - lastReliableIndex == 1) {
				lastReliableIndex++
				reliableWindowStart++
				reliableWindowEnd++
				handleEncapsulatedPacketRoute(packet)
				if (!reliableWindow.isEmpty()) {
					val sortedMap = TreeMap(reliableWindow)
					for (index in sortedMap.keys) {
						val pk = reliableWindow[index]
						if (index - lastReliableIndex != 1) {
							break
						}
						lastReliableIndex++
						reliableWindowStart++
						reliableWindowEnd++
						handleEncapsulatedPacketRoute(pk)
						reliableWindow.remove(index)
					}
				}
			} else {
				reliableWindow[packet.messageIndex] = packet
			}
		}
	}

	@Throws(Exception::class)
	private fun handleEncapsulatedPacketRoute(packet: EncapsulatedPacket?) {
		if (sessionManager == null) {
			return
		}
		if (packet!!.hasSplit) {
			if (state == STATE_CONNECTED) {
				handleSplit(packet)
			}
			return
		}
		val id = packet.buffer[0]
		if (id and 0xff < 0x80) { //internal data packet
			if (state == STATE_CONNECTING_2) {
				if (id == CLIENT_CONNECT_DataPacket.Companion.ID) {
					val dataPacket = CLIENT_CONNECT_DataPacket()
					dataPacket.buffer = packet.buffer
					dataPacket.decode()
					val pk = SERVER_HANDSHAKE_DataPacket()
					pk.address = address
					pk.port = port
					pk.sendPing = dataPacket.sendPing
					pk.sendPong = dataPacket.sendPing + 1000L
					pk.encode()
					val sendPacket = EncapsulatedPacket()
					sendPacket.reliability = 0
					sendPacket.buffer = pk.buffer!!
					addToQueue(sendPacket, RakNet.PRIORITY_IMMEDIATE.toInt())
				} else if (id == CLIENT_HANDSHAKE_DataPacket.Companion.ID) {
					val dataPacket = CLIENT_HANDSHAKE_DataPacket()
					dataPacket.buffer = packet.buffer
					dataPacket.decode()
					if (dataPacket.port == sessionManager.getPort() || !sessionManager!!.portChecking) {
						state = STATE_CONNECTED //FINALLY!
						isTemporal = false
						sessionManager!!.openSession(this)
					}
				}
			} else if (id == CLIENT_DISCONNECT_DataPacket.Companion.ID) {
				disconnect("client disconnect")
			} else if (id == PING_DataPacket.Companion.ID) {
				val dataPacket = PING_DataPacket()
				dataPacket.buffer = packet.buffer
				dataPacket.decode()
				val pk = PONG_DataPacket()
				pk.pingID = dataPacket.pingID
				pk.encode()
				var sendPacket = EncapsulatedPacket()
				sendPacket.reliability = 0
				sendPacket.buffer = pk.buffer!!
				addToQueue(sendPacket)

				//Latency measurement
				val pingPacket = PING_DataPacket()
				pingPacket.pingID = System.currentTimeMillis()
				pingPacket.encode()
				sendPacket = EncapsulatedPacket()
				sendPacket.reliability = 0
				sendPacket.buffer = pingPacket.buffer!!
				addToQueue(sendPacket)
			} else if (id == PONG_DataPacket.Companion.ID) {
				if (state == STATE_CONNECTED) {
					val dataPacket = PONG_DataPacket()
					dataPacket.buffer = packet.buffer
					dataPacket.decode()
					if (state == STATE_CONNECTED) {
						val pingPacket = PING_DataPacket()
						pingPacket.pingID = (System.currentTimeMillis() - dataPacket.pingID) / 10
						pingPacket.encode()
						packet.buffer = pingPacket.buffer!!
						sessionManager!!.streamEncapsulated(this, packet)
					}
				}
			}
		} else if (state == STATE_CONNECTED) {
			sessionManager!!.streamEncapsulated(this, packet)
		} else {
			//this.sessionManager.getLogger().notice("Received packet before connection: "+Binary.bytesToHexString(packet.buffer));
		}
	}

	@Throws(Exception::class)
	fun handlePacket(packet: Packet) {
		isActive = true
		lastUpdate = System.currentTimeMillis()
		if (state == STATE_CONNECTED || state == STATE_CONNECTING_2) {
			if ((packet.buffer!![0] and 0xff >= 0x80 || packet.buffer!![0] and 0xff <= 0x8f) && packet is DataPacket) {
				val dp = packet
				dp.decode()
				if (dp.seqNumber!! < windowStart || dp.seqNumber!! > windowEnd || receivedWindow.containsKey(dp.seqNumber)) {
					return
				}
				val diff = dp.seqNumber!! - lastSeqNumber
				NACKQueue.remove(dp.seqNumber)
				ACKQueue[dp.seqNumber] = dp.seqNumber
				receivedWindow[dp.seqNumber] = dp.seqNumber
				if (diff != 1) {
					for (i in lastSeqNumber + 1 until dp.seqNumber!!) {
						if (!receivedWindow.containsKey(i)) {
							NACKQueue[i] = i
						}
					}
				}
				if (diff >= 1) {
					lastSeqNumber = dp.seqNumber!!
					windowStart += diff
					windowEnd += diff
				}
				for (pk in dp.packets) {
					if (pk is EncapsulatedPacket) {
						handleEncapsulatedPacket(pk)
					}
				}
			} else {
				if (packet is ACK) {
					packet.decode()
					for (seq in ArrayList(packet.packets!!.values)) {
						if (recoveryQueue.containsKey(seq)) {
							for (pk in recoveryQueue[seq]!!.packets) {
								if (pk is EncapsulatedPacket && pk.needACK && pk.messageIndex != null) {
									if (needACK.containsKey(pk.identifierACK)) {
										needACK[pk.identifierACK]!!.remove(pk.messageIndex)
									}
								}
							}
							recoveryQueue.remove(seq)
						}
					}
				} else if (packet is NACK) {
					packet.decode()
					for (seq in ArrayList(packet.packets!!.values)) {
						if (recoveryQueue.containsKey(seq)) {
							val pk = recoveryQueue[seq]
							pk!!.seqNumber = sendSeqNumber++
							packetToSend.add(pk)
							recoveryQueue.remove(seq)
						}
					}
				}
			}
		} else if (packet.buffer!![0] and 0xff > 0x00 || packet.buffer!![0] and 0xff < 0x80) { //Not Data packet :)
			packet.decode()
			if (packet is OPEN_CONNECTION_REQUEST_1) {
				//TODO: check protocol number and refuse connections
				val pk = OPEN_CONNECTION_REPLY_1()
				pk.mtuSize = packet.mtuSize
				pk.serverID = sessionManager.getID()
				sendPacket(pk)
				state = STATE_CONNECTING_1
			} else if (state == STATE_CONNECTING_1 && packet is OPEN_CONNECTION_REQUEST_2) {
				iD = packet.clientID
				if (packet.serverPort == sessionManager.getPort() || !sessionManager!!.portChecking) {
					mtuSize = clamp(Math.abs(packet.mtuSize.toInt()), MIN_MTU_SIZE, MAX_MTU_SIZE)
					val pk = OPEN_CONNECTION_REPLY_2()
					pk.mtuSize = mtuSize.toShort()
					pk.serverID = sessionManager.getID()
					pk.clientAddress = address
					pk.clientPort = port
					sendPacket(pk)
					state = STATE_CONNECTING_2
				}
			}
		}
	}

	@Throws(Exception::class)
	fun close() {
		val data = byteArrayOf(0x60, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x15) //CLIENT_DISCONNECT packet 0x15
		addEncapsulatedToQueue(fromBinary(data))
		sessionManager = null
	}

	companion object {
		const val STATE_UNCONNECTED = 0
		const val STATE_CONNECTING_1 = 1
		const val STATE_CONNECTING_2 = 2
		const val STATE_CONNECTED = 3
		const val MAX_SPLIT_SIZE = 128
		const val MAX_SPLIT_COUNT = 4
		const val WINDOW_SIZE = 2048
		private const val MAX_MTU_SIZE = 1492
		private const val MIN_MTU_SIZE = 400
	}

	init {
		sendQueue = DATA_PACKET_4()
		lastUpdate = System.currentTimeMillis()
		startTime = System.currentTimeMillis()
		isActive = false
		windowStart = -1
		windowEnd = WINDOW_SIZE
		reliableWindowStart = 0
		reliableWindowEnd = WINDOW_SIZE
		for (i in 0..31) {
			channelIndex[i] = 0
		}
	}
}