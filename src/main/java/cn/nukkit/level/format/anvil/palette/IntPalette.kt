package cn.nukkit.level.format.anvil.palette

import java.util.*

/**
 * @author https://github.com/boy0001/
 */
class IntPalette {
	private var keys = INT0
	private var lastIndex = Int.MIN_VALUE
	fun add(key: Int) {
		keys = insert(key)
		lastIndex = Int.MIN_VALUE
	}

	protected fun set(keys: IntArray) {
		this.keys = keys
		lastIndex = Int.MIN_VALUE
	}

	private fun insert(`val`: Int): IntArray {
		lastIndex = Int.MIN_VALUE
		if (keys.size == 0) {
			return intArrayOf(`val`)
		} else if (`val` < keys[0]) {
			val s = IntArray(keys.size + 1)
			System.arraycopy(keys, 0, s, 1, keys.size)
			s[0] = `val`
			return s
		} else if (`val` > keys[keys.size - 1]) {
			val s = Arrays.copyOf(keys, keys.size + 1)
			s[keys.size] = `val`
			return s
		}
		val s = Arrays.copyOf(keys, keys.size + 1)
		for (i in s.indices) {
			if (keys[i] < `val`) {
				continue
			}
			System.arraycopy(keys, i, s, i + 1, s.size - i - 1)
			s[i] = `val`
			break
		}
		return s
	}

	fun getKey(index: Int): Int {
		return keys[index]
	}

	fun getValue(key: Int): Int {
		val lastTmp = lastIndex
		val hasLast = lastTmp != Int.MIN_VALUE
		val index: Int
		index = if (hasLast) {
			val lastKey = keys[lastTmp]
			if (lastKey == key) return lastTmp
			if (lastKey > key) {
				binarySearch0(0, lastTmp, key)
			} else {
				binarySearch0(lastTmp + 1, keys.size, key)
			}
		} else {
			binarySearch0(0, keys.size, key)
		}
		return if (index >= keys.size || index < 0) {
			Int.MIN_VALUE.also { lastIndex = it }
		} else {
			index.also { lastIndex = it }
		}
	}

	private fun binarySearch0(fromIndex: Int, toIndex: Int, key: Int): Int {
		var low = fromIndex
		var high = toIndex - 1
		while (low <= high) {
			val mid = low + high ushr 1
			val midVal = keys[mid]
			if (midVal < key) low = mid + 1 else if (midVal > key) high = mid - 1 else return mid // key found
		}
		return -(low + 1) // key not found.
	}

	fun length(): Int {
		return keys.size
	}

	override fun clone(): IntPalette {
		val p = IntPalette()
		p.keys = if (keys != INT0) keys.clone() else INT0
		p.lastIndex = this.lastIndex
		return p
	}

	companion object {
		private val INT0 = IntArray(0)
	}
}