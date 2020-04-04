package cn.nukkit.level.generator.populator.impl

import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom
import java.util.concurrent.ThreadLocalRandom

/**
 * @author Niall Lindsay (Niall7459)
 *
 *
 * Nukkit Project
 *
 */
class PopulatorTallSugarcane : PopulatorSugarcane() {
	override fun placeBlock(x: Int, y: Int, z: Int, id: Int, chunk: FullChunk, random: NukkitRandom?) {
		val height = ThreadLocalRandom.current().nextInt(3) + 1
		for (i in 0 until height) {
			chunk.setFullBlockId(x, y + i, z, id)
		}
	}
}