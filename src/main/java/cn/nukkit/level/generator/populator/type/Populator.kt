package cn.nukkit.level.generator.populator.type

import cn.nukkit.block.BlockID
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class Populator : BlockID {
	abstract fun populate(level: ChunkManager, chunkX: Int, chunkZ: Int, random: NukkitRandom, chunk: FullChunk)
	protected open fun getHighestWorkableBlock(level: ChunkManager?, x: Int, z: Int, chunk: FullChunk): Int {
		return chunk.getHighestBlockAt(x and 0xF, z and 0xF)
	}
}