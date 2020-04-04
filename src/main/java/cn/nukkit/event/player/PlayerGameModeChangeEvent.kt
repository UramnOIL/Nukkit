package cn.nukkit.event.player

import cn.nukkit.AdventureSettings
import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class PlayerGameModeChangeEvent(player: Player?, newGameMode: Int, newAdventureSettings: AdventureSettings) : PlayerEvent(), Cancellable {
	val newGamemode: Int
	var newAdventureSettings: AdventureSettings

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		newGamemode = newGameMode
		this.newAdventureSettings = newAdventureSettings
	}
}