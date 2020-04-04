package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.lang.TextContainer

class PlayerKickEvent(player: Player?, reason: Reason, reasonString: String?, quitMessage: TextContainer?) : PlayerEvent(), Cancellable {
	enum class Reason {
		NEW_CONNECTION, KICKED_BY_ADMIN, NOT_WHITELISTED, IP_BANNED, NAME_BANNED, INVALID_PVE, LOGIN_TIMEOUT, SERVER_FULL, FLYING_DISABLED, UNKNOWN;

		override fun toString(): String {
			return name
		}
	}

	var quitMessage: TextContainer?
	var reasonEnum: Reason
	val reason: String

	@Deprecated("")
	constructor(player: Player?, reason: String?, quitMessage: String?) : this(player, Reason.UNKNOWN, reason, TextContainer(quitMessage)) {
	}

	@Deprecated("")
	constructor(player: Player?, reason: String?, quitMessage: TextContainer?) : this(player, Reason.UNKNOWN, reason, quitMessage) {
	}

	constructor(player: Player?, reason: Reason, quitMessage: TextContainer?) : this(player, reason, reason.toString(), quitMessage) {}
	constructor(player: Player?, reason: Reason, quitMessage: String?) : this(player, reason, TextContainer(quitMessage)) {}

	fun setQuitMessage(joinMessage: String?) {
		quitMessage = TextContainer(joinMessage)
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.quitMessage = quitMessage
		reasonEnum = reason
		reasonEnum = reason.name
	}
}