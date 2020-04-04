package cn.nukkit.item.randomitem

import java.util.function.Consumer

/**
 * Created by Snake1999 on 2016/1/15.
 * Package cn.nukkit.item.randomitem in project nukkit.
 */
open class Selector(parent: Selector?) {
	var parent: Selector? = null
		private set

	fun setParent(parent: Selector?): Selector? {
		this.parent = parent
		return parent
	}

	open fun select(): Any {
		return this
	}

	companion object {
		fun selectRandom(selectorChanceMap: Map<Selector?, Float>): Selector? {
			val totalChance = floatArrayOf(0f)
			selectorChanceMap.values.forEach(Consumer { f: Float -> totalChance[0] += f })
			val resultChance = (Math.random() * totalChance[0]).toFloat()
			val flag = floatArrayOf(0f)
			val found = booleanArrayOf(false)
			val temp = arrayOf<Selector?>(null)
			selectorChanceMap.forEach { (o: Selector?, f: Float) ->
				flag[0] += f
				if (flag[0] > resultChance && !found[0]) {
					temp[0] = o
					found[0] = true
				}
			}
			return temp[0]
		}
	}

	init {
		setParent(parent)
	}
}