package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList

class PlayerFoodLevelChangeEvent(player: Player?, foodLevel: Int, foodSaturationLevel: Float) : PlayerEvent(), Cancellable {
	var foodLevel: Int
	var foodSaturationLevel: Float

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.foodLevel = foodLevel
		this.foodSaturationLevel = foodSaturationLevel
	}
}