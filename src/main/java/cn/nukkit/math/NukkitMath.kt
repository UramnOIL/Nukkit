package cn.nukkit.math

/**
 * author: MagicDroidX
 * Nukkit Project
 */
object NukkitMath {
	fun floorDouble(n: Double): Int {
		val i = n.toInt()
		return if (n >= i) i else i - 1
	}

	fun ceilDouble(n: Double): Int {
		val i = (n + 1).toInt()
		return if (n >= i) i else i - 1
	}

	fun floorFloat(n: Float): Int {
		val i = n.toInt()
		return if (n >= i) i else i - 1
	}

	fun ceilFloat(n: Float): Int {
		val i = (n + 1).toInt()
		return if (n >= i) i else i - 1
	}

	@JvmOverloads
	fun randomRange(random: NukkitRandom, start: Int = 0): Int {
		return randomRange(random, 0, 0x7fffffff)
	}

	fun randomRange(random: NukkitRandom, start: Int, end: Int): Int {
		return start + random.nextInt() % (end + 1 - start)
	}

	@JvmOverloads
	fun round(d: Double, precision: Int = 0): Double {
		return Math.round(d * Math.pow(10.0, precision.toDouble())).toDouble() / Math.pow(10.0, precision.toDouble())
	}

	fun clamp(value: Double, min: Double, max: Double): Double {
		return if (value < min) min else if (value > max) max else value
	}

	fun clamp(value: Int, min: Int, max: Int): Int {
		return if (value < min) min else if (value > max) max else value
	}

	fun getDirection(diffX: Double, diffZ: Double): Double {
		var diffX = diffX
		var diffZ = diffZ
		diffX = Math.abs(diffX)
		diffZ = Math.abs(diffZ)
		return if (diffX > diffZ) diffX else diffZ
	}
}