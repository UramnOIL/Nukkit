package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class PlayerAchievementAwardedEvent(player: Player?, achievementId: String) : PlayerEvent(), Cancellable {
	val achievement: String

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		achievement = achievementId
	}
}