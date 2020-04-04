package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.HandlerList
import cn.nukkit.lang.TextContainer

class PlayerQuitEvent @JvmOverloads constructor(player: Player?, quitMessage: TextContainer?, autoSave: Boolean = true, reason: String? = "No reason") : PlayerEvent() {
	var quitMessage: TextContainer? = null
	var autoSave = true
	var reason: String? = null
		protected set

	constructor(player: Player?, quitMessage: TextContainer?, reason: String?) : this(player, quitMessage, true, reason) {}
	constructor(player: Player?, quitMessage: String?, reason: String?) : this(player, quitMessage, true, reason) {}
	constructor(player: Player?, quitMessage: String?, autoSave: Boolean, reason: String?) : this(player, TextContainer(quitMessage), autoSave, reason) {}

	@JvmOverloads
	constructor(player: Player?, quitMessage: String?, autoSave: Boolean = true) : this(player, TextContainer(quitMessage), autoSave) {
	}

	fun setQuitMessage(quitMessage: String?) {
		this.quitMessage = TextContainer(quitMessage)
	}

	fun setAutoSave() {
		autoSave = true
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.quitMessage = quitMessage
		this.autoSave = autoSave
		this.reason = reason
	}
}