package cn.nukkit.level.generator.noise.nukkit.f

import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom

/**
 * author: DaPorkchop_
 * Nukkit Project
 */
open class PerlinF @JvmOverloads constructor(random: NukkitRandom, octaves: Float, persistence: Float, expansion: Float = 1f) : NoiseF() {
	override fun getNoise2D(x: Float, y: Float): Float {
		return getNoise3D(x, y, 0f)
	}

	override fun getNoise3D(x: Float, y: Float, z: Float): Float {
		var x = x
		var y = y
		var z = z
		x += offsetX
		y += offsetY
		z += offsetZ
		val floorX = x.toInt()
		val floorY = y.toInt()
		val floorZ = z.toInt()
		val X = floorX and 0xFF
		val Y = floorY and 0xFF
		val Z = floorZ and 0xFF
		x -= floorX.toFloat()
		y -= floorY.toFloat()
		z -= floorZ.toFloat()

		//Fade curves
		//fX = fade(x);
		//fY = fade(y);
		//fZ = fade(z);
		val fX = x * x * x * (x * (x * 6 - 15) + 10)
		val fY = y * y * y * (y * (y * 6 - 15) + 10)
		val fZ = z * z * z * (z * (z * 6 - 15) + 10)

		//Cube corners
		val A = perm[X] + Y
		val B = perm[X + 1] + Y
		val AA = perm[A] + Z
		val AB = perm[A + 1] + Z
		val BA = perm[B] + Z
		val BB = perm[B + 1] + Z
		val AA1: Float = NoiseF.Companion.grad(perm[AA], x, y, z)
		val BA1: Float = NoiseF.Companion.grad(perm[BA], x - 1, y, z)
		val AB1: Float = NoiseF.Companion.grad(perm[AB], x, y - 1, z)
		val BB1: Float = NoiseF.Companion.grad(perm[BB], x - 1, y - 1, z)
		val AA2: Float = NoiseF.Companion.grad(perm[AA + 1], x, y, z - 1)
		val BA2: Float = NoiseF.Companion.grad(perm[BA + 1], x - 1, y, z - 1)
		val AB2: Float = NoiseF.Companion.grad(perm[AB + 1], x, y - 1, z - 1)
		val BB2: Float = NoiseF.Companion.grad(perm[BB + 1], x - 1, y - 1, z - 1)
		val xLerp11 = AA1 + fX * (BA1 - AA1)
		val zLerp1 = xLerp11 + fY * (AB1 + fX * (BB1 - AB1) - xLerp11)
		val xLerp21 = AA2 + fX * (BA2 - AA2)
		return zLerp1 + fZ * (xLerp21 + fY * (AB2 + fX * (BB2 - AB2) - xLerp21) - zLerp1)
	}

	init {
		this.octaves = octaves
		this.persistence = persistence
		this.expansion = expansion
		offsetX = random.nextFloat() * 256
		offsetY = random.nextFloat() * 256
		offsetZ = random.nextFloat() * 256
		perm = IntArray(512)
		for (i in 0..255) {
			perm[i] = random.nextBoundedInt(256)
		}
		for (i in 0..255) {
			val pos = random.nextBoundedInt(256 - i) + i
			val old = perm[i]
			perm[i] = perm[pos]
			perm[pos] = old
			perm[i + 256] = perm[i]
		}
	}
}