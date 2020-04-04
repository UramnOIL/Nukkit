package cn.nukkit.network.rcon

import cn.nukkit.Server
import cn.nukkit.command.RemoteConsoleCommandSender
import cn.nukkit.event.server.RemoteServerCommandEvent
import cn.nukkit.utils.TextFormat
import java.io.IOException
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * Implementation of Source RCON protocol.
 * https://developer.valvesoftware.com/wiki/Source_RCON_Protocol
 *
 *
 * Wrapper for RCONServer. Handles data.
 *
 * @author Tee7even
 */
class RCON(server: Server?, password: String?, address: String?, port: Int) {
	private val server: Server?
	private var serverThread: RCONServer? = null
	fun check() {
		if (serverThread == null) {
			return
		} else if (!serverThread.isAlive()) {
			return
		}
		var command: RCONCommand?
		while (serverThread.receive().also({ command = it }) != null) {
			val sender = RemoteConsoleCommandSender()
			val event = RemoteServerCommandEvent(sender, command.getCommand())
			server.pluginManager.callEvent(event)
			if (!event.isCancelled()) {
				server.dispatchCommand(sender, command.getCommand())
			}
			serverThread.respond(command!!.getSender(), command.getId(), TextFormat.clean(sender.getMessages()))
		}
	}

	fun close() {
		try {
			synchronized(serverThread) {
				serverThread!!.close()
				serverThread.wait(5000)
			}
		} catch (exception: InterruptedException) {
			//
		}
	}

	init {
		if (password.isEmpty()) {
			throw IllegalArgumentException("nukkit.server.rcon.emptyPasswordError")
		}
		this.server = server
		try {
			serverThread = RCONServer(address, port, password)
			serverThread.start()
		} catch (e: IOException) {
			throw IllegalArgumentException("nukkit.server.rcon.startupError", e)
		}
		this.server.getLogger().info(this.server.getLanguage().translateString("nukkit.server.rcon.running", arrayOf(address, String.valueOf(port))))
	}
}