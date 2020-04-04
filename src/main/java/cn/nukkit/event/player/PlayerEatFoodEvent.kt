package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.item.food.Food

/**
 * Created by Snake1999 on 2016/1/14.
 * Package cn.nukkit.event.player in project nukkit.
 */
class PlayerEatFoodEvent(player: Player?, food: Food) : PlayerEvent(), Cancellable {
	var food: Food

	companion object {
		val handlers = HandlerList()
	}

	init {
		this.player = player
		this.food = food
	}
}