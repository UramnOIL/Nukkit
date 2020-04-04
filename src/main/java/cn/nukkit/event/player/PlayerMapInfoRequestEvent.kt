package cn.nukkit.event.player

import cn.nukkit.Player
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.item.Item

/**
 * Created by CreeperFace on 18.3.2017.
 */
class PlayerMapInfoRequestEvent(p: Player?, item: Item) : PlayerEvent(), Cancellable {
	val map: Item

	companion object {
		val handlers = HandlerList()
	}

	init {
		player = p
		map = item
	}
}