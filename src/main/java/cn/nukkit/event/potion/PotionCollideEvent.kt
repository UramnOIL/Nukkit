package cn.nukkit.event.potion

import cn.nukkit.entity.item.EntityPotion
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.potion.Potion

/**
 * Created by Snake1999 on 2016/1/12.
 * Package cn.nukkit.event.potion in project nukkit
 */
class PotionCollideEvent(potion: Potion, val thrownPotion: EntityPotion) : PotionEvent(potion), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}