package cn.nukkit.level.generator.noise.nukkit.f

import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom

/**
 * author: DaPorkchop_
 * Nukkit Project
 */
class SimplexF : PerlinF {
	protected val offsetW: Float

	constructor(random: NukkitRandom, octaves: Float, persistence: Float) : super(random, octaves, persistence) {
		offsetW = random.nextFloat() * 256
		SQRT_3 = Math.sqrt(3.0).toFloat()
		SQRT_5 = Math.sqrt(5.0).toFloat()
		F2 = 0.5f * (SQRT_3 - 1f)
		G2 = (3f - SQRT_3) / 6f
		G22 = G2 * 2.0f - 1f
		F3 = 1.0f / 3.0f
		G3 = 1.0f / 6.0f
		F4 = (SQRT_5 - 1.0f) / 4.0f
		G4 = (5.0f - SQRT_5) / 20.0f
		G42 = G4 * 2.0f
		G43 = G4 * 3.0f
		G44 = G4 * 4.0f - 1.0f
	}

	constructor(random: NukkitRandom, octaves: Float, persistence: Float, expansion: Float) : super(random, octaves, persistence, expansion) {
		offsetW = random.nextFloat() * 256
		SQRT_3 = Math.sqrt(3.0).toFloat()
		SQRT_5 = Math.sqrt(5.0).toFloat()
		F2 = 0.5f * (SQRT_3 - 1f)
		G2 = (3f - SQRT_3) / 6f
		G22 = G2 * 2.0f - 1f
		F3 = 1.0f / 3.0f
		G3 = 1.0f / 6.0f
		F4 = (SQRT_5 - 1.0f) / 4.0f
		G4 = (5.0f - SQRT_5) / 20.0f
		G42 = G4 * 2.0f
		G43 = G4 * 3.0f
		G44 = G4 * 4.0f - 1.0f
	}

	override fun getNoise3D(x: Float, y: Float, z: Float): Float {
		var x = x
		var y = y
		var z = z
		x += offsetX
		y += offsetY
		z += offsetZ

		// Skew the input space to determine which simplex cell we're in
		val s = (x + y + z) * F3 // Very nice and simple skew factor for 3D
		val i = (x + s).toInt()
		val j = (y + s).toInt()
		val k = (z + s).toInt()
		val t = (i + j + k) * G3
		// Unskew the cell origin back to (x,y,z) space
		val x0 = x - (i - t) // The x,y,z distances from the cell origin
		val y0 = y - (j - t)
		val z0 = z - (k - t)

		// For the 3D case, the simplex shape is a slightly irregular tetrahedron.
		var i1 = 0
		var j1 = 0
		var k1 = 0
		var i2 = 0
		var j2 = 0
		var k2 = 0

		// Determine which simplex we are in.
		if (x0 >= y0) {
			if (y0 >= z0) {
				i1 = 1
				j1 = 0
				k1 = 0
				i2 = 1
				j2 = 1
				k2 = 0
			} // X Y Z order
			else if (x0 >= z0) {
				i1 = 1
				j1 = 0
				k1 = 0
				i2 = 1
				j2 = 0
				k2 = 1
			} // X Z Y order
			else {
				i1 = 0
				j1 = 0
				k1 = 1
				i2 = 1
				j2 = 0
				k2 = 1
			}
			// Z X Y order
		} else { // x0<y0
			if (y0 < z0) {
				i1 = 0
				j1 = 0
				k1 = 1
				i2 = 0
				j2 = 1
				k2 = 1
			} // Z Y X order
			else if (x0 < z0) {
				i1 = 0
				j1 = 1
				k1 = 0
				i2 = 0
				j2 = 1
				k2 = 1
			} // Y Z X order
			else {
				i1 = 0
				j1 = 1
				k1 = 0
				i2 = 1
				j2 = 1
				k2 = 0
			}
			// Y X Z order
		}

		// A step of (1,0,0) in (i,j,k) means a step of (1-c,-c,-c) in (x,y,z),
		// a step of (0,1,0) in (i,j,k) means a step of (-c,1-c,-c) in (x,y,z), and
		// a step of (0,0,1) in (i,j,k) means a step of (-c,-c,1-c) in (x,y,z), where
		// c = 1/6.
		val x1 = x0 - i1 + G3 // Offsets for second corner in (x,y,z) coords
		val y1 = y0 - j1 + G3
		val z1 = z0 - k1 + G3
		val x2 = x0 - i2 + 2.0f * G3 // Offsets for third corner in (x,y,z) coords
		val y2 = y0 - j2 + 2.0f * G3
		val z2 = z0 - k2 + 2.0f * G3
		val x3 = x0 - 1.0f + 3.0f * G3 // Offsets for last corner in (x,y,z) coords
		val y3 = y0 - 1.0f + 3.0f * G3
		val z3 = z0 - 1.0f + 3.0f * G3

		// Work out the hashed gradient indices of the four simplex corners
		val ii = i and 255
		val jj = j and 255
		val kk = k and 255
		var n = 0f

		// Calculate the contribution from the four corners
		val t0 = 0.6f - x0 * x0 - y0 * y0 - z0 * z0
		if (t0 > 0) {
			val gi0 = grad3[perm[ii + perm[jj + perm[kk]]] % 12]
			n += t0 * t0 * t0 * t0 * (gi0[0] * x0 + gi0[1] * y0 + gi0[2] * z0)
		}
		val t1 = 0.6f - x1 * x1 - y1 * y1 - z1 * z1
		if (t1 > 0) {
			val gi1 = grad3[perm[ii + i1 + perm[jj + j1 + perm[kk + k1]]] % 12]
			n += t1 * t1 * t1 * t1 * (gi1[0] * x1 + gi1[1] * y1 + gi1[2] * z1)
		}
		val t2 = 0.6f - x2 * x2 - y2 * y2 - z2 * z2
		if (t2 > 0) {
			val gi2 = grad3[perm[ii + i2 + perm[jj + j2 + perm[kk + k2]]] % 12]
			n += t2 * t2 * t2 * t2 * (gi2[0] * x2 + gi2[1] * y2 + gi2[2] * z2)
		}
		val t3 = 0.6f - x3 * x3 - y3 * y3 - z3 * z3
		if (t3 > 0) {
			val gi3 = grad3[perm[ii + 1 + perm[jj + 1 + perm[kk + 1]]] % 12]
			n += t3 * t3 * t3 * t3 * (gi3[0] * x3 + gi3[1] * y3 + gi3[2] * z3)
		}

		// Add contributions from each corner to get the noise value.
		// The result is scaled to stay just inside [-1,1]
		return 32.0f * n
	}

	override fun getNoise2D(x: Float, y: Float): Float {
		var x = x
		var y = y
		x += offsetX
		y += offsetY

		// Skew the input space to determine which simplex cell we're in
		val s = (x + y) * F2 // Hairy factor for 2D
		val i = (x + s).toInt()
		val j = (y + s).toInt()
		val t = (i + j) * G2
		// Unskew the cell origin back to (x,y) space
		val x0 = x - (i - t) // The x,y distances from the cell origin
		val y0 = y - (j - t)

		// For the 2D case, the simplex shape is an equilateral triangle.
		var i1 = 0
		var j1 = 0
		// Determine which simplex we are in.
		if (x0 > y0) {
			i1 = 1
			j1 = 0
		} // lower triangle, XY order: (0,0).(1,0).(1,1)
		else {
			i1 = 0
			j1 = 1
		}
		// upper triangle, YX order: (0,0).(0,1).(1,1)

		// A step of (1,0) in (i,j) means a step of (1-c,-c) in (x,y), and
		// a step of (0,1) in (i,j) means a step of (-c,1-c) in (x,y), where
		// c = (3-sqrt(3))/6
		val x1 = x0 - i1 + G2 // Offsets for middle corner in (x,y) unskewed coords
		val y1 = y0 - j1 + G2
		val x2 = x0 + G22 // Offsets for last corner in (x,y) unskewed coords
		val y2 = y0 + G22

		// Work out the hashed gradient indices of the three simplex corners
		val ii = i and 255
		val jj = j and 255
		var n = 0f

		// Calculate the contribution from the three corners
		val t0 = 0.5f - x0 * x0 - y0 * y0
		if (t0 > 0) {
			val gi0 = grad3[perm[ii + perm[jj]] % 12]
			n += t0 * t0 * t0 * t0 * (gi0[0] * x0 + gi0[1] * y0) // (x,y) of grad3 used for 2D gradient
		}
		val t1 = 0.5f - x1 * x1 - y1 * y1
		if (t1 > 0) {
			val gi1 = grad3[perm[ii + i1 + perm[jj + j1]] % 12]
			n += t1 * t1 * t1 * t1 * (gi1[0] * x1 + gi1[1] * y1)
		}
		val t2 = 0.5f - x2 * x2 - y2 * y2
		if (t2 > 0) {
			val gi2 = grad3[perm[ii + 1 + perm[jj + 1]] % 12]
			n += t2 * t2 * t2 * t2 * (gi2[0] * x2 + gi2[1] * y2)
		}

		// Add contributions from each corner to get the noise value.
		// The result is scaled to return values in the interval [-1,1].
		return 70.0f * n
	}

	companion object {
		protected var SQRT_3: Float
		protected var SQRT_5: Float
		protected var F2: Float
		protected var G2: Float
		protected var G22: Float
		protected var F3: Float
		protected var G3: Float
		protected var F4: Float
		protected var G4: Float
		protected var G42: Float
		protected var G43: Float
		protected var G44: Float
		val grad3 = arrayOf(intArrayOf(1, 1, 0), intArrayOf(-1, 1, 0), intArrayOf(1, -1, 0), intArrayOf(-1, -1, 0), intArrayOf(1, 0, 1), intArrayOf(-1, 0, 1), intArrayOf(1, 0, -1), intArrayOf(-1, 0, -1), intArrayOf(0, 1, 1), intArrayOf(0, -1, 1), intArrayOf(0, 1, -1), intArrayOf(0, -1, -1))
		protected fun dot2D(g: IntArray, x: Float, y: Float): Float {
			return g[0] * x + g[1] * y
		}

		protected fun dot3D(g: IntArray, x: Float, y: Float, z: Float): Float {
			return g[0] * x + g[1] * y + g[2] * z
		}

		protected fun dot4D(g: IntArray, x: Float, y: Float, z: Float, w: Float): Float {
			return g[0] * x + g[1] * y + g[2] * z + g[3] * w
		}
	}
}