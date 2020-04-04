package cn.nukkit.level

import cn.nukkit.math.MathHelper.floor
import cn.nukkit.math.Vector3

/**
 * Author: Adam Matthew
 *
 *
 * Nukkit Project
 */
class ChunkPosition(val x: Int, val y: Int, val z: Int) {

	constructor(vec3d: Vector3) : this(floor(vec3d.x), floor(vec3d.y), floor(vec3d.z)) {}

	override fun equals(`object`: Any?): Boolean {
		return if (`object` !is ChunkPosition) {
			false
		} else {
			val chunkposition = `object`
			chunkposition.x == x && chunkposition.y == y && chunkposition.z == z
		}
	}

	override fun hashCode(): Int {
		return x * 8976890 + y * 981131 + z
	}

}