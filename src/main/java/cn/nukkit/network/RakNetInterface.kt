package cn.nukkit.network

import cn.nukkit.Nukkit
import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.event.player.PlayerCreationEvent
import cn.nukkit.event.server.QueryRegenerateEvent
import cn.nukkit.network.protocol.BatchPacket
import cn.nukkit.network.protocol.DataPacket
import cn.nukkit.network.protocol.ProtocolInfo
import cn.nukkit.raknet.RakNet
import cn.nukkit.raknet.protocol.EncapsulatedPacket
import cn.nukkit.raknet.protocol.packet.PING_DataPacket
import cn.nukkit.raknet.server.RakNetServer
import cn.nukkit.raknet.server.ServerHandler
import cn.nukkit.raknet.server.ServerInstance
import cn.nukkit.utils.Binary
import cn.nukkit.utils.MainLogger
import cn.nukkit.utils.Utils
import cn.nukkit.utils.Zlib
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class RakNetInterface(server: Server?) : ServerInstance, AdvancedSourceInterface {
	private val server: Server?
	private var network: Network? = null
	private val raknet: RakNetServer?
	private val players: Map<String?, Player?>? = ConcurrentHashMap()
	private val networkLatency: Map<String?, Integer?>? = ConcurrentHashMap()
	private val identifiers: Map<Integer?, String?>? = ConcurrentHashMap()
	private val identifiersACK: Map<String?, Integer?>? = ConcurrentHashMap()
	private val handler: ServerHandler?
	private val channelCounts: IntArray? = IntArray(256)

	@Override
	override fun setNetwork(network: Network?) {
		this.network = network
	}

	@Override
	override fun process(): Boolean {
		var work = false
		if (handler.handlePacket()) {
			work = true
			while (handler.handlePacket()) {
			}
		}
		return work
	}

	@Override
	fun closeSession(identifier: String?, reason: String?) {
		if (players!!.containsKey(identifier)) {
			val player: Player? = players[identifier]
			identifiers.remove(player.rawHashCode())
			players.remove(identifier)
			networkLatency.remove(identifier)
			identifiersACK.remove(identifier)
			player.close(player.getLeaveMessage(), reason)
		}
	}

	@Override
	override fun getNetworkLatency(player: Player?): Int {
		return networkLatency!![identifiers!![player.rawHashCode()]]
	}

	@Override
	override fun close(player: Player?) {
		this.close(player, "unknown reason")
	}

	@Override
	override fun close(player: Player?, reason: String?) {
		if (identifiers!!.containsKey(player.rawHashCode())) {
			val id = identifiers[player.rawHashCode()]
			players.remove(id)
			networkLatency.remove(id)
			identifiersACK.remove(id)
			closeSession(id, reason)
			identifiers.remove(player.rawHashCode())
		}
	}

	@Override
	override fun shutdown() {
		handler.shutdown()
	}

	@Override
	override fun emergencyShutdown() {
		handler.emergencyShutdown()
	}

	@Override
	fun openSession(identifier: String?, address: String?, port: Int, clientID: Long) {
		val ev = PlayerCreationEvent(this, Player::class.java, Player::class.java, null, address, port)
		server.pluginManager.callEvent(ev)
		val clazz: Class<out Player?> = ev.getPlayerClass()
		try {
			val constructor: Constructor = clazz.getConstructor(SourceInterface::class.java, Long::class.java, String::class.java, Int::class.javaPrimitiveType)
			val player: Player = constructor.newInstance(this, ev.getClientId(), ev.getAddress(), ev.getPort()) as Player
			players.put(identifier, player)
			networkLatency.put(identifier, 0)
			identifiersACK.put(identifier, 0)
			identifiers.put(player.rawHashCode(), identifier)
			server.addPlayer(identifier, player)
		} catch (e: NoSuchMethodException) {
			Server.instance.getLogger().logException(e)
		} catch (e: InvocationTargetException) {
			Server.instance.getLogger().logException(e)
		} catch (e: InstantiationException) {
			Server.instance.getLogger().logException(e)
		} catch (e: IllegalAccessException) {
			Server.instance.getLogger().logException(e)
		}
	}

	@Override
	fun handleEncapsulated(identifier: String?, packet: EncapsulatedPacket?, flags: Int) {
		if (players!!.containsKey(identifier)) {
			var pk: DataPacket? = null
			try {
				if (packet.buffer.length > 0) {
					if (packet.buffer.get(0) === PING_DataPacket.iD) {
						val pingPacket = PING_DataPacket()
						pingPacket.buffer = packet.buffer
						pingPacket.decode()
						networkLatency.put(identifier, pingPacket.pingID as Int)
						return
					}
					pk = getPacket(packet.buffer)
					if (pk != null) {
						pk.decode()
						players[identifier].handleDataPacket(pk)
					}
				}
			} catch (e: Exception) {
				server.getLogger().logException(e)
				if (Nukkit.DEBUG > 1 && pk != null) {
					val logger: MainLogger = server.getLogger()
					//                    if (logger != null) {
					logger.debug("Packet " + pk.getClass().getName().toString() + " 0x" + Binary.bytesToHexString(packet.buffer))
					//logger.logException(e);
//                    }
				}
				if (players.containsKey(identifier)) {
					handler.blockAddress(players[identifier].getAddress(), 5)
				}
			}
		}
	}

	@Override
	override fun blockAddress(address: String?) {
		this.blockAddress(address, 300)
	}

	@Override
	override fun blockAddress(address: String?, timeout: Int) {
		handler.blockAddress(address, timeout)
	}

	@Override
	override fun unblockAddress(address: String?) {
		handler.unblockAddress(address)
	}

	@Override
	fun handleRaw(address: String?, port: Int, payload: ByteArray?) {
		server.handlePacket(address, port, payload)
	}

	@Override
	override fun sendRawPacket(address: String?, port: Int, payload: ByteArray?) {
		handler.sendRaw(address, port, payload)
	}

	@Override
	fun notifyACK(identifier: String?, identifierACK: Int) {
		// TODO: Better ACK notification implementation!
		for (p in server.getOnlinePlayers().values()) {
			p.notifyACK(identifierACK)
		}
	}

	@Override
	override fun setName(name: String?) {
		val info: QueryRegenerateEvent = server.getQueryInformation()
		val names: Array<String?> = name.split("!@#") //Split double names within the program
		handler.sendOption("name",
				"MCPE;" + Utils.rtrim(names[0].replace(";", "\\;"), '\\').toString() + ";" +
						ProtocolInfo.CURRENT_PROTOCOL.toString() + ";" +
						ProtocolInfo.MINECRAFT_VERSION_NETWORK.toString() + ";" +
						info.getPlayerCount().toString() + ";" +
						info.getMaxPlayerCount().toString() + ";" +
						server.getServerUniqueId().toString().toString() + ";" +
						(if (names.size > 1) Utils.rtrim(names[1].replace(";", "\\;"), '\\') else "").toString() + ";" +
						Server.getGamemodeString(server.getDefaultGamemode(), true).toString() + ";")
	}

	fun setPortCheck(value: Boolean) {
		handler.sendOption("portChecking", String.valueOf(value))
	}

	@Override
	fun handleOption(name: String?, value: String?) {
		if ("bandwidth".equals(name)) {
			val v: Array<String?> = value.split(";")
			network!!.addStatistics(Double.valueOf(v[0]), Double.valueOf(v[1]))
		}
	}

	@Override
	override fun putPacket(player: Player?, packet: DataPacket?): Integer? {
		return this.putPacket(player, packet, false)
	}

	@Override
	override fun putPacket(player: Player?, packet: DataPacket?, needACK: Boolean): Integer? {
		return this.putPacket(player, packet, needACK, false)
	}

	@Override
	override fun putPacket(player: Player?, packet: DataPacket?, needACK: Boolean, immediate: Boolean): Integer? {
		if (identifiers!!.containsKey(player.rawHashCode())) {
			var buffer: ByteArray
			if (packet!!.pid() === ProtocolInfo.BATCH_PACKET) {
				buffer = (packet as BatchPacket?)!!.payload
			} else if (!needACK) {
				server.batchPackets(arrayOf<Player?>(player), arrayOf<DataPacket?>(packet), true)
				return null
			} else {
				if (!packet!!.isEncoded) {
					packet!!.encode()
					packet!!.isEncoded = true
				}
				buffer = packet.getBuffer()
				buffer = try {
					Zlib.deflate(
							Binary.appendBytes(Binary.writeUnsignedVarInt(buffer.size), buffer),
							Server.instance.networkCompressionLevel)
				} catch (e: Exception) {
					throw RuntimeException(e)
				}
			}
			val identifier = identifiers[player.rawHashCode()]
			var pk: EncapsulatedPacket? = null
			if (!needACK) {
				if (packet!!.encapsulatedPacket == null) {
					packet!!.encapsulatedPacket = CacheEncapsulatedPacket()
					packet!!.encapsulatedPacket.identifierACK = null
					packet!!.encapsulatedPacket.buffer = Binary.appendBytes(0xfe.toByte(), buffer)
					if (packet.getChannel() !== 0) {
						packet!!.encapsulatedPacket.reliability = 3
						packet!!.encapsulatedPacket.orderChannel = packet.getChannel()
						packet!!.encapsulatedPacket.orderIndex = 0
					} else {
						packet!!.encapsulatedPacket.reliability = 2
					}
				}
				pk = packet!!.encapsulatedPacket
			}
			if (pk == null) {
				pk = EncapsulatedPacket()
				pk.buffer = Binary.appendBytes(0xfe.toByte(), buffer)
				if (packet.getChannel() !== 0) {
					packet!!.reliability = 3
					packet!!.orderChannel = packet.getChannel()
					packet!!.orderIndex = 0
				} else {
					packet!!.reliability = 2
				}
				if (needACK) {
					var iACK: Int = identifiersACK!![identifier]
					iACK++
					pk.identifierACK = iACK
					identifiersACK.put(identifier, iACK)
				}
			}
			handler.sendEncapsulated(identifier, pk, (if (needACK) RakNet.FLAG_NEED_ACK else 0) or if (immediate) RakNet.PRIORITY_IMMEDIATE else RakNet.PRIORITY_NORMAL)
			return pk.identifierACK
		}
		return null
	}

	private fun getPacket(buffer: ByteArray?): DataPacket? {
		var start = 0
		if (buffer!![0] == 0xfe.toByte()) {
			start++
		}
		val data: DataPacket = network!!.getPacket(ProtocolInfo.BATCH_PACKET) ?: return null
		data.setBuffer(buffer, start)
		return data
	}

	init {
		this.server = server
		raknet = RakNetServer(this.server.getLogger(), this.server.getPort(), if (this.server.getIp().equals("")) "0.0.0.0" else this.server.getIp())
		handler = ServerHandler(raknet, this)
	}
}