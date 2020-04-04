package cn.nukkit.event.player

import cn.nukkit.Server
import cn.nukkit.event.HandlerList
import java.util.*
import java.util.function.Consumer

/**
 * This event is called asynchronously
 *
 * @author CreeperFace
 */
class PlayerAsyncPreLoginEvent(val name: String, val uuid: UUID, val address: String, val port: Int) : PlayerEvent() {
	var loginResult = LoginResult.SUCCESS
	var kickMessage = "Plugin Reason"
	private val scheduledActions: MutableList<Consumer<Server>> = ArrayList()

	fun scheduleSyncAction(action: Consumer<Server>) {
		scheduledActions.add(action)
	}

	fun getScheduledActions(): List<Consumer<Server>> {
		return ArrayList(scheduledActions)
	}

	fun allow() {
		loginResult = LoginResult.SUCCESS
	}

	fun disAllow(message: String) {
		loginResult = LoginResult.KICK
		kickMessage = message
	}

	enum class LoginResult {
		SUCCESS, KICK
	}

	companion object {
		val handlers = HandlerList()
	}

}