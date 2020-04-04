package cn.nukkit.math

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class Vector2f @JvmOverloads constructor(val x: Float = 0f, val y: Float = 0f) {

	val floorX: Int
		get() = NukkitMath.floorFloat(x)

	val floorY: Int
		get() = NukkitMath.floorFloat(y)

	@JvmOverloads
	fun add(x: Float, y: Float = 0f): Vector2f {
		return Vector2f(this.x + x, this.y + y)
	}

	fun add(x: Vector2f): Vector2f {
		return this.add(x.x, x.y)
	}

	@JvmOverloads
	fun subtract(x: Float, y: Float = 0f): Vector2f {
		return this.add(-x, -y)
	}

	fun subtract(x: Vector2f): Vector2f {
		return this.add(-x.x, -x.y)
	}

	fun ceil(): Vector2f {
		return Vector2f((x + 1).toInt(), (y + 1).toInt())
	}

	fun floor(): Vector2f {
		return Vector2f(floorX.toFloat(), floorY.toFloat())
	}

	fun round(): Vector2f {
		return Vector2f(Math.round(x).toFloat(), Math.round(y).toFloat())
	}

	fun abs(): Vector2f {
		return Vector2f(Math.abs(x), Math.abs(y))
	}

	fun multiply(number: Float): Vector2f {
		return Vector2f(x * number, y * number)
	}

	fun divide(number: Float): Vector2f {
		return Vector2f(x / number, y / number)
	}

	@JvmOverloads
	fun distance(x: Float, y: Float = 0f): Double {
		return Math.sqrt(this.distanceSquared(x, y))
	}

	fun distance(vector: Vector2f): Double {
		return Math.sqrt(this.distanceSquared(vector.x, vector.y))
	}

	@JvmOverloads
	fun distanceSquared(x: Float, y: Float = 0f): Double {
		return Math.pow(this.x - x.toDouble(), 2.0) + Math.pow(this.y - y.toDouble(), 2.0)
	}

	fun distanceSquared(vector: Vector2f): Double {
		return this.distanceSquared(vector.x, vector.y)
	}

	fun length(): Double {
		return Math.sqrt(lengthSquared().toDouble())
	}

	fun lengthSquared(): Float {
		return x * x + y * y
	}

	fun normalize(): Vector2f {
		val len = lengthSquared()
		return if (len != 0f) {
			divide(Math.sqrt(len.toDouble()).toFloat())
		} else Vector2f(0, 0)
	}

	fun dot(v: Vector2f): Float {
		return x * v.x + y * v.y
	}

	override fun toString(): String {
		return "Vector2(x=" + x + ",y=" + y + ")"
	}

}