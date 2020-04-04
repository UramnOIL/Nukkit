package cn.nukkit.math

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class Vector3 @JvmOverloads constructor(var south: Double = 0.0, var up: Double = 0.0, var west: Double = 0.0) : Cloneable {

	val floorX: Int
		get() = Math.floor(south).toInt()

	val floorY: Int
		get() = Math.floor(up).toInt()

	val floorZ: Int
		get() = Math.floor(west).toInt()

	val chunkX: Int
		get() = floorX shr 4

	val chunkZ: Int
		get() = floorZ shr 4

	open fun add(x: Double): Vector3? {
		return this.add(x, 0.0, 0.0)
	}

	open fun add(x: Double, y: Double): Vector3? {
		return this.add(x, y, 0.0)
	}

	open fun add(x: Double, y: Double, z: Double): Vector3? {
		return Vector3(south + x, up + y, west + z)
	}

	open fun add(x: Vector3): Vector3? {
		return Vector3(south + x.south, up + x.up, west + x.west)
	}

	open fun subtract(): Vector3? {
		return this.subtract(0.0, 0.0, 0.0)
	}

	open fun subtract(x: Double): Vector3? {
		return this.subtract(x, 0.0, 0.0)
	}

	open fun subtract(x: Double, y: Double): Vector3? {
		return this.subtract(x, y, 0.0)
	}

	open fun subtract(x: Double, y: Double, z: Double): Vector3? {
		return this.add(-x, -y, -z)
	}

	open fun subtract(x: Vector3): Vector3? {
		return this.add(-x.south, -x.up, -x.west)
	}

	open fun multiply(number: Double): Vector3? {
		return Vector3(south * number, up * number, west * number)
	}

	open fun divide(number: Double): Vector3 {
		return Vector3(south / number, up / number, west / number)
	}

	open fun ceil(): Vector3? {
		return Vector3(Math.ceil(south).toInt(), Math.ceil(up).toInt(), Math.ceil(west).toInt())
	}

	open fun floor(): Vector3? {
		return Vector3(floorX.toDouble(), floorY.toDouble(), floorZ.toDouble())
	}

	open fun round(): Vector3? {
		return Vector3(Math.round(south).toDouble(), Math.round(up).toDouble(), Math.round(west).toDouble())
	}

	open fun abs(): Vector3? {
		return Vector3(Math.abs(south).toInt(), Math.abs(up).toInt(), Math.abs(west).toInt())
	}

	open fun getSide(face: BlockFace): Vector3? {
		return this.getSide(face, 1)
	}

	open fun getSide(face: BlockFace, step: Int): Vector3? {
		return Vector3(south + face.xOffset * step, up + face.yOffset * step, west + face.zOffset * step)
	}

	open fun up(): Vector3? {
		return up(1)
	}

	open fun up(step: Int): Vector3? {
		return getSide(BlockFace.UP, step)
	}

	open fun down(): Vector3? {
		return down(1)
	}

	open fun down(step: Int): Vector3? {
		return getSide(BlockFace.DOWN, step)
	}

	open fun north(): Vector3? {
		return north(1)
	}

	open fun north(step: Int): Vector3? {
		return getSide(BlockFace.NORTH, step)
	}

	open fun south(): Vector3? {
		return south(1)
	}

	open fun south(step: Int): Vector3? {
		return getSide(BlockFace.SOUTH, step)
	}

	open fun east(): Vector3? {
		return east(1)
	}

	open fun east(step: Int): Vector3? {
		return getSide(BlockFace.EAST, step)
	}

	open fun west(): Vector3? {
		return west(1)
	}

	open fun west(step: Int): Vector3? {
		return getSide(BlockFace.WEST, step)
	}

	fun distance(pos: Vector3): Double {
		return Math.sqrt(distanceSquared(pos))
	}

	fun distanceSquared(pos: Vector3): Double {
		return Math.pow(south - pos.south, 2.0) + Math.pow(up - pos.up, 2.0) + Math.pow(west - pos.west, 2.0)
	}

	@JvmOverloads
	fun maxPlainDistance(x: Double = 0.0, z: Double = 0.0): Double {
		return Math.max(Math.abs(south - x), Math.abs(west - z))
	}

	fun maxPlainDistance(vector: Vector2): Double {
		return this.maxPlainDistance(vector.x, vector.y)
	}

	fun maxPlainDistance(x: Vector3): Double {
		return this.maxPlainDistance(x.south, x.west)
	}

	fun length(): Double {
		return Math.sqrt(lengthSquared())
	}

	fun lengthSquared(): Double {
		return south * south + up * up + west * west
	}

	fun normalize(): Vector3 {
		val len = lengthSquared()
		return if (len > 0) {
			divide(Math.sqrt(len))
		} else Vector3(0, 0, 0)
	}

	fun dot(v: Vector3): Double {
		return south * v.south + up * v.up + west * v.west
	}

	fun cross(v: Vector3): Vector3 {
		return Vector3(
				up * v.west - west * v.up,
				west * v.south - south * v.west,
				south * v.up - up * v.south
		)
	}

	/**
	 * Returns a new vector with x value equal to the second parameter, along the line between this vector and the
	 * passed in vector, or null if not possible.
	 *
	 * @param v vector
	 * @param x x value
	 * @return intermediate vector
	 */
	fun getIntermediateWithXValue(v: Vector3, x: Double): Vector3? {
		val xDiff = v.south - south
		val yDiff = v.up - up
		val zDiff = v.west - west
		if (xDiff * xDiff < 0.0000001) {
			return null
		}
		val f = (x - south) / xDiff
		return if (f < 0 || f > 1) {
			null
		} else {
			Vector3(south + xDiff * f, up + yDiff * f, west + zDiff * f)
		}
	}

	/**
	 * Returns a new vector with y value equal to the second parameter, along the line between this vector and the
	 * passed in vector, or null if not possible.
	 *
	 * @param v vector
	 * @param y y value
	 * @return intermediate vector
	 */
	fun getIntermediateWithYValue(v: Vector3, y: Double): Vector3? {
		val xDiff = v.south - south
		val yDiff = v.up - up
		val zDiff = v.west - west
		if (yDiff * yDiff < 0.0000001) {
			return null
		}
		val f = (y - up) / yDiff
		return if (f < 0 || f > 1) {
			null
		} else {
			Vector3(south + xDiff * f, up + yDiff * f, west + zDiff * f)
		}
	}

	/**
	 * Returns a new vector with z value equal to the second parameter, along the line between this vector and the
	 * passed in vector, or null if not possible.
	 *
	 * @param v vector
	 * @param z z value
	 * @return intermediate vector
	 */
	fun getIntermediateWithZValue(v: Vector3, z: Double): Vector3? {
		val xDiff = v.south - south
		val yDiff = v.up - up
		val zDiff = v.west - west
		if (zDiff * zDiff < 0.0000001) {
			return null
		}
		val f = (z - west) / zDiff
		return if (f < 0 || f > 1) {
			null
		} else {
			Vector3(south + xDiff * f, up + yDiff * f, west + zDiff * f)
		}
	}

	open fun setComponents(x: Double, y: Double, z: Double): Vector3? {
		south = x
		up = y
		west = z
		return this
	}

	override fun toString(): String {
		return "Vector3(x=" + south + ",y=" + up + ",z=" + west + ")"
	}

	override fun equals(obj: Any?): Boolean {
		if (obj !is Vector3) {
			return false
		}
		val other = obj
		return south == other.south && up == other.up && west == other.west
	}

	override fun hashCode(): Int {
		return south.toInt() xor (west.toInt() shl 12) xor (up.toInt() shl 24)
	}

	fun rawHashCode(): Int {
		return super.hashCode()
	}

	public override fun clone(): Vector3 {
		return try {
			super.clone() as Vector3
		} catch (e: CloneNotSupportedException) {
			null
		}
	}

	fun asVector3f(): Vector3f {
		return Vector3f(south.toFloat(), up.toFloat(), west.toFloat())
	}

	fun asBlockVector3(): BlockVector3 {
		return BlockVector3(floorX, floorY, floorZ)
	}

}