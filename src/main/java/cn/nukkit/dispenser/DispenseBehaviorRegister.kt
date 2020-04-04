package cn.nukkit.dispenser

import java.util.*

/**
 * @author CreeperFace
 */
object DispenseBehaviorRegister {
	private val behaviors: MutableMap<Int, DispenseBehavior> = HashMap()
	private val defaultBehavior: DispenseBehavior = DefaultDispenseBehavior()
	fun registerBehavior(itemId: Int, behavior: DispenseBehavior) {
		behaviors[itemId] = behavior
	}

	fun getBehavior(id: Int): DispenseBehavior {
		return behaviors.getOrDefault(id, defaultBehavior)
	}

	fun removeDispenseBehavior(id: Int) {
		behaviors.remove(id)
	}
}