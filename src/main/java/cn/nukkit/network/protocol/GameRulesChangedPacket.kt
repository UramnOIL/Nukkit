package cn.nukkit.network.protocol

import cn.nukkit.level.GameRules
import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
class GameRulesChangedPacket : DataPacket() {
	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	var gameRules: GameRules? = null

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		putGameRules(gameRules)
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.GAME_RULES_CHANGED_PACKET
	}
}