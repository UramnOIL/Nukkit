package cn.nukkit.math

/**
 * author: MagicDroidX
 * Nukkit Project
 */
object VectorMath {
	fun getDirection2D(azimuth: Double): Vector2 {
		return Vector2(Math.cos(azimuth), Math.sin(azimuth))
	}
}