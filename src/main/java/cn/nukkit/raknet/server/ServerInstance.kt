package cn.nukkit.raknet.server

import cn.nukkit.raknet.protocol.EncapsulatedPacket

/**
 * author: MagicDroidX
 * Nukkit Project
 */
interface ServerInstance {
	fun openSession(identifier: String?, address: String?, port: Int, clientID: Long)
	fun closeSession(identifier: String?, reason: String?)
	fun handleEncapsulated(identifier: String?, packet: EncapsulatedPacket?, flags: Int)
	fun handleRaw(address: String?, port: Int, payload: ByteArray?)
	fun notifyACK(identifier: String?, identifierACK: Int)
	fun handleOption(option: String?, value: String?)
}