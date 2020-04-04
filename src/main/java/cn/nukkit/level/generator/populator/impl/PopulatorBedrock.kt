package cn.nukkit.level.generator.populator.impl

import cn.nukkit.block.BlockID
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.level.generator.populator.type.Populator
import cn.nukkit.math.NukkitRandom

/**
 * @author DaPorkchop_
 *
 * Places bedrock on the bottom of the world
 */
class PopulatorBedrock : Populator() {
	override fun populate(level: ChunkManager, chunkX: Int, chunkZ: Int, random: NukkitRandom, chunk: FullChunk) {
		for (x in 0..15) {
			for (z in 0..15) {
				chunk.setBlockId(x, 0, z, BlockID.BEDROCK)
				for (i in 1..4) {
					if (random.nextBoundedInt(i) == 0) { //decreasing amount
						chunk.setBlockId(x, i, z, BlockID.BEDROCK)
					}
				}
			}
		}
	}
}