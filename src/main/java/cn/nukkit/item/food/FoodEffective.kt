package cn.nukkit.item.food

import cn.nukkit.Player
import cn.nukkit.potion.Effect
import java.util.*
import java.util.function.Consumer
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.set

/**
 * Created by Snake1999 on 2016/1/13.
 * Package cn.nukkit.item.food in project nukkit.
 */
class FoodEffective(restoreFood: Int, restoreSaturation: Float) : Food() {
	protected val effects: MutableMap<Effect, Float> = LinkedHashMap()
	fun addEffect(effect: Effect): FoodEffective {
		return addChanceEffect(1f, effect)
	}

	fun addChanceEffect(chance: Float, effect: Effect): FoodEffective {
		var chance = chance
		if (chance > 1f) chance = 1f
		if (chance < 0f) chance = 0f
		effects[effect] = chance
		return this
	}

	override fun onEatenBy(player: Player): Boolean {
		super.onEatenBy(player)
		val toApply: MutableList<Effect> = LinkedList()
		effects.forEach { (effect: Effect, chance: Float) -> if (chance >= Math.random()) toApply.add(effect.clone()) }
		toApply.forEach(Consumer { effect: Effect? -> player.addEffect(effect) })
		return true
	}

	init {
		setRestoreFood(restoreFood)
		setRestoreSaturation(restoreSaturation)
	}
}