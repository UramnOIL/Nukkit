package cn.nukkit.network

import cn.nukkit.Player
import cn.nukkit.network.protocol.DataPacket
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
interface SourceInterface {
	fun putPacket(player: Player?, packet: DataPacket?): Integer?
	fun putPacket(player: Player?, packet: DataPacket?, needACK: Boolean): Integer?
	fun putPacket(player: Player?, packet: DataPacket?, needACK: Boolean, immediate: Boolean): Integer?
	fun getNetworkLatency(player: Player?): Int
	fun close(player: Player?)
	fun close(player: Player?, reason: String?)
	fun setName(name: String?)
	fun process(): Boolean
	fun shutdown()
	fun emergencyShutdown()
}