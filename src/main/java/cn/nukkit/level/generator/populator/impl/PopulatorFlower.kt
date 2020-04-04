package cn.nukkit.level.generator.populator.impl

import cn.nukkit.block.BlockID
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.level.generator.populator.helper.EnsureCover
import cn.nukkit.level.generator.populator.helper.EnsureGrassBelow
import cn.nukkit.level.generator.populator.type.PopulatorSurfaceBlock
import cn.nukkit.math.NukkitRandom
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * @author Angelic47, Niall Lindsay (Niall7459)
 *
 *
 * Nukkit Project
 *
 */
class PopulatorFlower : PopulatorSurfaceBlock() {
	private val flowerTypes: MutableList<IntArray> = ArrayList()
	fun addType(a: Int, b: Int) {
		val c = IntArray(2)
		c[0] = a
		c[1] = b
		flowerTypes.add(c)
	}

	val types: List<IntArray>
		get() = flowerTypes

	override fun placeBlock(x: Int, y: Int, z: Int, id: Int, chunk: FullChunk, random: NukkitRandom?) {
		if (flowerTypes.size != 0) {
			val type = flowerTypes[ThreadLocalRandom.current().nextInt(flowerTypes.size)]
			chunk.setFullBlockId(x, y, z, type[0] shl 4 or type[1])
			if (type[0] == BlockID.DOUBLE_PLANT) {
				chunk.setFullBlockId(x, y + 1, z, type[0] shl 4 or (8 or type[1]))
			}
		}
	}

	override fun canStay(x: Int, y: Int, z: Int, chunk: FullChunk): Boolean {
		return EnsureCover.Companion.ensureCover(x, y, z, chunk) && EnsureGrassBelow.Companion.ensureGrassBelow(x, y, z, chunk)
	}

	override fun getBlockId(x: Int, z: Int, random: NukkitRandom?, chunk: FullChunk?): Int {
		return 0
	}
}