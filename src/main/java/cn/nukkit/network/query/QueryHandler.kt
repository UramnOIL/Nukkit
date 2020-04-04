package cn.nukkit.network.query

import cn.nukkit.Server
import cn.nukkit.event.server.QueryRegenerateEvent
import cn.nukkit.utils.Binary
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Random
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class QueryHandler {
	private val server: Server?
	private var lastToken: ByteArray?
	private var token: ByteArray?
	private var longData: ByteArray?
	private var shortData: ByteArray?
	private var timeout: Long = 0
	fun regenerateInfo() {
		val ev: QueryRegenerateEvent = server.getQueryInformation()
		longData = ev.getLongQuery(longData)
		shortData = ev.getShortQuery(shortData)
		timeout = System.currentTimeMillis() + ev.getTimeout()
	}

	fun regenerateToken() {
		lastToken = token
		val token = ByteArray(16)
		for (i in 0..15) {
			token[i] = Random().nextInt(255) as Byte
		}
		this.token = token
	}

	fun handle(address: String?, port: Int, packet: ByteArray?) {
		var offset = 2 //skip MAGIC
		val packetType = packet!![offset++]
		val sessionID: Int = Binary.readInt(Binary.subBytes(packet, offset, 4))
		offset += 4
		val payload: ByteArray = Binary.subBytes(packet, offset)
		when (packetType) {
			HANDSHAKE -> {
				val reply: ByteArray = Binary.appendBytes(
						HANDSHAKE,
						Binary.writeInt(sessionID),
						getTokenString(token, address).getBytes(), byteArrayOf(0x00))
				server.network.sendPacket(address, port, reply)
			}
			STATISTICS -> {
				val token: String = String.valueOf(Binary.readInt(Binary.subBytes(payload, 0, 4)))
				if (!token.equals(getTokenString(this.token, address)) && !token.equals(getTokenString(lastToken, address))) {
					break
				}
				if (timeout < System.currentTimeMillis()) {
					regenerateInfo()
				}
				reply = Binary.appendBytes(
						STATISTICS,
						Binary.writeInt(sessionID),
						if (payload.size == 8) longData else shortData
				)
				server.network.sendPacket(address, port, reply)
			}
		}
	}

	companion object {
		const val HANDSHAKE: Byte = 0x09
		const val STATISTICS: Byte = 0x00
		fun getTokenString(token: ByteArray?, salt: String?): String? {
			return getTokenString(String(token), salt)
		}

		fun getTokenString(token: String?, salt: String?): String? {
			return try {
				String.valueOf(Binary.readInt(Binary.subBytes(MessageDigest.getInstance("SHA-512").digest((salt.toString() + ":" + token).getBytes()), 7, 4)))
			} catch (e: NoSuchAlgorithmException) {
				String.valueOf(Random().nextInt())
			}
		}
	}

	init {
		server = Server.instance
		server.getLogger().info(server.getLanguage().translateString("nukkit.server.query.start"))
		val ip: String = server.getIp()
		val addr = if (!ip.isEmpty()) ip else "0.0.0.0"
		val port: Int = server.getPort()
		server.getLogger().info(server.getLanguage().translateString("nukkit.server.query.info", String.valueOf(port)))
		regenerateToken()
		lastToken = token
		regenerateInfo()
		server.getLogger().info(server.getLanguage().translateString("nukkit.server.query.running", arrayOf<String?>(addr, String.valueOf(port))))
	}
}