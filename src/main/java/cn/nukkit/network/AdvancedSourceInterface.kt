package cn.nukkit.network

import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
interface AdvancedSourceInterface : SourceInterface {
	fun blockAddress(address: String?)
	fun blockAddress(address: String?, timeout: Int)
	fun unblockAddress(address: String?)
	fun setNetwork(network: Network?)
	fun sendRawPacket(address: String?, port: Int, payload: ByteArray?)
}