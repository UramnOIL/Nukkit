package cn.nukkit.level.generator.populator.impl

import cn.nukkit.block.Block
import cn.nukkit.block.BlockSapling
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.level.generator.`object`.tree.ObjectTree
import cn.nukkit.level.generator.populator.type.PopulatorCount
import cn.nukkit.math.NukkitMath.randomRange
import cn.nukkit.math.NukkitRandom

/**
 * author: DaPorkchop_
 * Nukkit Project
 */
class PopulatorTree @JvmOverloads constructor(private val type: Int = BlockSapling.OAK) : PopulatorCount() {
	private var level: ChunkManager? = null
	public override fun populateCount(level: ChunkManager, chunkX: Int, chunkZ: Int, random: NukkitRandom, chunk: FullChunk) {
		this.level = level
		val x = randomRange(random, chunkX shl 4, (chunkX shl 4) + 15)
		val z = randomRange(random, chunkZ shl 4, (chunkZ shl 4) + 15)
		val y = this.getHighestWorkableBlock(x, z)
		if (y < 3) {
			return
		}
		ObjectTree.Companion.growTree(this.level, x, y, z, random, type)
	}

	private fun getHighestWorkableBlock(x: Int, z: Int): Int {
		var y: Int
		y = 254
		while (y > 0) {
			val b = level!!.getBlockIdAt(x, y, z)
			if (b == Block.DIRT || b == Block.GRASS) {
				break
			} else if (b != Block.AIR && b != Block.SNOW_LAYER) {
				return -1
			}
			--y
		}
		return ++y
	}

}