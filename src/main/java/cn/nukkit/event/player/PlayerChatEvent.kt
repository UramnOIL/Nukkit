package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.Server
import cn.nukkit.command.CommandSender
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import java.util.*

class PlayerChatEvent @JvmOverloads constructor(player: Player?, message: String?, format: String = "chat.type.text", recipients: MutableSet<CommandSender>? = null) : PlayerMessageEvent(), Cancellable {
	var format: String
	protected var recipients: MutableSet<CommandSender> = HashSet()

	/**
	 * Changes the player that is sending the message
	 *
	 * @param player messenger
	 */
	override var player: Player?
		get() = super.player

	fun getRecipients(): Set<CommandSender> {
		return recipients
	}

	fun setRecipients(recipients: MutableSet<CommandSender>) {
		this.recipients = recipients
	}

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.message = message
		this.format = format
		if (recipients == null) {
			for (permissible in Server.instance!!.pluginManager.getPermissionSubscriptions(Server.BROADCAST_CHANNEL_USERS)) {
				if (permissible is CommandSender) {
					this.recipients.add(permissible)
				}
			}
		} else {
			this.recipients = recipients
		}
	}
}