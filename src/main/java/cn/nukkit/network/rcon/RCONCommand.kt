package cn.nukkit.network.rcon

import java.nio.channels.SocketChannel
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * A data structure to hold sender, request ID and command itself.
 *
 * @author Tee7even
 */
class RCONCommand(sender: SocketChannel?, id: Int, command: String?) {
	private val sender: SocketChannel?
	val id: Int
	val command: String?
	fun getSender(): SocketChannel? {
		return sender
	}

	init {
		this.sender = sender
		this.id = id
		this.command = command
	}
}