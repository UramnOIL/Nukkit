package cn.nukkit.network.protocol.types

import lombok.ToString
import java.util.OptionalLong
import java.util.UUID
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * @author SupremeMortal
 * Nukkit project
 */
@ToString
class CommandOriginData(val type: Origin?, uuid: UUID?, requestId: String?, varlong: Long?) {
	val uuid: UUID?
	val requestId: String?
	private val varlong: Long?
	val varLong: OptionalLong?
		get() = if (varlong == null) {
			OptionalLong.empty()
		} else OptionalLong.of(varlong)

	enum class Origin {
		PLAYER, BLOCK, MINECART_BLOCK, DEV_CONSOLE, TEST, AUTOMATION_PLAYER, CLIENT_AUTOMATION, DEDICATED_SERVER, ENTITY, VIRTUAL, GAME_ARGUMENT, ENTITY_SERVER
	}

	init {
		this.uuid = uuid
		this.requestId = requestId
		this.varlong = varlong
	}
}