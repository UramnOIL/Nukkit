package cn.nukkit.level.generator.populator.impl

import cn.nukkit.block.BlockID
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.biome.EnumBiome
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.level.generator.populator.type.Populator
import cn.nukkit.math.NukkitRandom

class WaterIcePopulator : Populator() {
	override fun populate(level: ChunkManager, chunkX: Int, chunkZ: Int, random: NukkitRandom, chunk: FullChunk) {
		for (x in 0..15) {
			for (z in 0..15) {
				val biome = EnumBiome.getBiome(chunk.getBiomeId(x, z))
				if (biome.isFreezing) {
					val topBlock = chunk.getHighestBlockAt(x, z)
					if (chunk.getBlockId(x, topBlock, z) == BlockID.STILL_WATER) {
						chunk.setBlockId(x, topBlock, z, BlockID.ICE)
					}
				}
			}
		}
	}
}