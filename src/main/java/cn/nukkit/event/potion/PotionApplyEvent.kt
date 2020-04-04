package cn.nukkit.event.potion

import cn.nukkit.entity.Entity
import cn.nukkit.event.Cancellable
import cn.nukkit.event.HandlerList
import cn.nukkit.potion.Effect
import cn.nukkit.potion.Potion

/**
 * Created by Snake1999 on 2016/1/12.
 * Package cn.nukkit.event.potion in project nukkit
 */
class PotionApplyEvent(potion: Potion, var applyEffect: Effect, val entity: Entity) : PotionEvent(potion), Cancellable {

	companion object {
		val handlers = HandlerList()
	}

}