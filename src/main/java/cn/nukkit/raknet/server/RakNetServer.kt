package cn.nukkit.raknet.server

import cn.nukkit.Server
import cn.nukkit.utils.ThreadedLogger
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class RakNetServer @JvmOverloads constructor(logger: ThreadedLogger, val port: Int, interfaz: String = "0.0.0.0") : Thread() {
	var `interface`: String
		protected set
	var logger: ThreadedLogger
		protected set
	var externalQueue: ConcurrentLinkedQueue<ByteArray>
		protected set
	var internalQueue: ConcurrentLinkedQueue<ByteArray>
		protected set
	var isShutdown = false
		protected set

	fun shutdown() {
		isShutdown = true
	}

	fun pushMainToThreadPacket(data: ByteArray) {
		internalQueue.add(data)
	}

	fun readMainToThreadPacket(): ByteArray {
		return internalQueue.poll()
	}

	fun pushThreadToMainPacket(data: ByteArray) {
		externalQueue.add(data)
	}

	fun readThreadToMainPacket(): ByteArray {
		return externalQueue.poll()
	}

	private inner class ShutdownHandler : Thread() {
		override fun run() {
			if (!isShutdown) {
				logger.emergency("RakNet crashed!")
			}
		}
	}

	override fun run() {
		name = "RakNet Thread #" + currentThread().id
		Runtime.getRuntime().addShutdownHook(ShutdownHandler())
		val socket = UDPServerSocket(logger, port, `interface`)
		try {
			SessionManager(this, socket)
		} catch (e: Exception) {
			Server.instance.logger.logException(e)
		}
	}

	init {
		require(!(port < 1 || port > 65536)) { "Invalid port range" }
		`interface` = interfaz
		this.logger = logger
		externalQueue = ConcurrentLinkedQueue()
		internalQueue = ConcurrentLinkedQueue()
		start()
	}
}