package cn.nukkit.level.generator.populator.type

import cn.nukkit.level.ChunkManager
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom

/**
 * @author DaPorkchop_
 *
 * A populator that generates an object a certain amount of times.
 * This prevents the exact same code from being repeated in nearly every single populator
 */
abstract class PopulatorCount : Populator() {
	private var randomAmount = 0
	private var baseAmount = 0
	fun setRandomAmount(randomAmount: Int) {
		this.randomAmount = randomAmount + 1
	}

	fun setBaseAmount(baseAmount: Int) {
		this.baseAmount = baseAmount
	}

	override fun populate(level: ChunkManager, chunkX: Int, chunkZ: Int, random: NukkitRandom, chunk: FullChunk) {
		val count = baseAmount + random.nextBoundedInt(randomAmount)
		for (i in 0 until count) {
			populateCount(level, chunkX, chunkZ, random, chunk)
		}
	}

	protected abstract fun populateCount(level: ChunkManager, chunkX: Int, chunkZ: Int, random: NukkitRandom, chunk: FullChunk)
}