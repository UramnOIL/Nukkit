package cn.nukkit.level.generator.populator.impl

import cn.nukkit.block.Block
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.level.generator.populator.type.Populator
import cn.nukkit.math.NukkitMath.randomRange
import cn.nukkit.math.NukkitRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class PopulatorOre @JvmOverloads constructor(private val replaceId: Int = Block.STONE) : Populator() {
	private var oreTypes: Array<OreType?> = arrayOfNulls<OreType>(0)
	override fun populate(level: ChunkManager, chunkX: Int, chunkZ: Int, random: NukkitRandom, chunk: FullChunk) {
		val sx = chunkX shl 4
		val ex = sx + 15
		val sz = chunkZ shl 4
		val ez = sz + 15
		for (type in oreTypes) {
			for (i in 0 until type.clusterCount) {
				val x = randomRange(random, sx, ex)
				val z = randomRange(random, sz, ez)
				val y = randomRange(random, type.minHeight, type.maxHeight)
				if (level.getBlockIdAt(x, y, z) != replaceId) {
					continue
				}
				type.spawn(level, random, replaceId, x, y, z)
			}
		}
	}

	fun setOreTypes(oreTypes: Array<OreType?>) {
		this.oreTypes = oreTypes
	}

}