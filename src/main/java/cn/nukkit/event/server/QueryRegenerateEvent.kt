package cn.nukkit.event.server

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.event.HandlerList
import cn.nukkit.nbt.stream.FastByteArrayOutputStream
import cn.nukkit.plugin.Plugin
import cn.nukkit.utils.Binary
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.Map
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.set
import kotlin.collections.toTypedArray

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class QueryRegenerateEvent @JvmOverloads constructor(server: Server, var timeout: Int = 5) : ServerEvent() {
	var serverName: String
	private var listPlugins: Boolean
	var plugins: Array<Plugin>
	var playerList: Array<Player>
	private val gameType: String
	private val version: String
	private val server_engine: String
	var world: String
	var playerCount: Int
	var maxPlayerCount: Int
	private val whitelist: String
	private val port: Int
	private val ip: String
	var extraData: Map<String, String> = HashMap()

	fun canListPlugins(): Boolean {
		return listPlugins
	}

	fun setListPlugins(listPlugins: Boolean) {
		this.listPlugins = listPlugins
	}

	fun getLongQuery(buffer: ByteArray?): ByteArray {
		var buffer = buffer
		if (buffer == null) buffer = ByteArray(Character.MAX_VALUE.toInt())
		val query = FastByteArrayOutputStream(buffer)
		try {
			var plist = server_engine
			if (plugins.size > 0 && listPlugins) {
				plist += ":"
				for (p in plugins) {
					val d = p.description
					plist += " " + d.name.replace(";", "").replace(":", "").replace(" ", "_") + " " + d.version.replace(";", "").replace(":", "").replace(" ", "_") + ";"
				}
				plist = plist.substring(0, plist.length - 2)
			}
			query.write("splitnum".toByteArray())
			query.write(0x00 as Byte.toInt())
			query.write(128 as Byte.toInt())
			query.write(0x00 as Byte.toInt())
			val KVdata = LinkedHashMap<String, String>()
			KVdata["hostname"] = serverName
			KVdata["gametype"] = gameType
			KVdata["game_id"] = GAME_ID
			KVdata["version"] = version
			KVdata["server_engine"] = server_engine
			KVdata["plugins"] = plist
			KVdata["map"] = world
			KVdata["numplayers"] = playerCount.toString()
			KVdata["maxplayers"] = maxPlayerCount.toString()
			KVdata["whitelist"] = whitelist
			KVdata["hostip"] = ip
			KVdata["hostport"] = port.toString()
			for ((key, value) in KVdata) {
				query.write(key.toByteArray(StandardCharsets.UTF_8))
				query.write(0x00 as Byte.toInt())
				query.write(value.toByteArray(StandardCharsets.UTF_8))
				query.write(0x00 as Byte.toInt())
			}
			query.write(byteArrayOf(0x00, 0x01))
			query.write("player_".toByteArray())
			query.write(byteArrayOf(0x00, 0x00))
			for (player in playerList) {
				query.write(player.getName().toByteArray(StandardCharsets.UTF_8))
				query.write(0x00 as Byte.toInt())
			}
			query.write(0x00 as Byte.toInt())
		} catch (e: IOException) {
			e.printStackTrace()
		}
		return query.toByteArray()
	}

	fun getShortQuery(buffer: ByteArray?): ByteArray {
		var buffer = buffer
		if (buffer == null) buffer = ByteArray(Character.MAX_VALUE.toInt())
		val query = FastByteArrayOutputStream(buffer)
		try {
			query.write(serverName.toByteArray(StandardCharsets.UTF_8))
			query.write(0x00 as Byte.toInt())
			query.write(gameType.toByteArray(StandardCharsets.UTF_8))
			query.write(0x00 as Byte.toInt())
			query.write(world.toByteArray(StandardCharsets.UTF_8))
			query.write(0x00 as Byte.toInt())
			query.write(playerCount.toString().toByteArray(StandardCharsets.UTF_8))
			query.write(0x00 as Byte.toInt())
			query.write(maxPlayerCount.toString().toByteArray(StandardCharsets.UTF_8))
			query.write(0x00 as Byte.toInt())
			query.write(Binary.writeLShort(port))
			query.write(ip.toByteArray(StandardCharsets.UTF_8))
			query.write(0x00 as Byte.toInt())
		} catch (e: IOException) {
			e.printStackTrace()
		}
		return query.toByteArray()
	}

	companion object {
		//alot todo
		val handlers = HandlerList()

		private const val GAME_ID = "MINECRAFTPE"
	}

	init {
		serverName = server.motd
		listPlugins = server.config
		plugins = server.pluginManager.plugins.values.toTypedArray()
		playerList = server.onlinePlayers.values.toTypedArray()
		gameType = if (server.gamemode and 0x01 == 0) "SMP" else "CMP"
		version = server.version
		server_engine = server.name + " " + server.nukkitVersion
		world = if (server.defaultLevel == null) "unknown" else server.defaultLevel!!.name
		playerCount = playerList.size
		maxPlayerCount = server.maxPlayers
		whitelist = if (server.hasWhitelist()) "on" else "off"
		port = server.port
		ip = server.ip
	}
}