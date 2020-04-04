package cn.nukkit.item.randomitem

import java.util.*
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.set

/**
 * Created by Snake1999 on 2016/1/15.
 * Package cn.nukkit.item.randomitem in project nukkit.
 */
object RandomItem {
	private val selectors: MutableMap<Selector?, Float> = HashMap()
	val ROOT = Selector(null)

	@JvmOverloads
	fun putSelector(selector: Selector?, chance: Float = 1f): Selector? {
		if (selector.getParent() == null) selector!!.parent = ROOT
		selectors[selector] = chance
		return selector
	}

	fun selectFrom(selector: Selector?): Any? {
		Objects.requireNonNull(selector)
		val child: MutableMap<Selector?, Float> = HashMap()
		selectors.forEach { (s: Selector?, f: Float) -> if (s.getParent() === selector) child[s] = f }
		return if (child.size == 0) selector!!.select() else selectFrom(Selector.Companion.selectRandom(child))
	}
}