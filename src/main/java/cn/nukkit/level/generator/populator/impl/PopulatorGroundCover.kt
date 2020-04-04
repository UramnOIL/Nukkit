package cn.nukkit.level.generator.populator.impl

import cn.nukkit.block.BlockID
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.biome.EnumBiome
import cn.nukkit.level.biome.type.CoveredBiome
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.level.generator.Normal
import cn.nukkit.level.generator.populator.type.Populator
import cn.nukkit.math.NukkitRandom

/**
 * author: DaPorkchop_
 * Nukkit Project
 */
class PopulatorGroundCover : Populator() {
	override fun populate(level: ChunkManager, chunkX: Int, chunkZ: Int, random: NukkitRandom, chunk: FullChunk) {
		val realX = chunkX shl 4
		val realZ = chunkZ shl 4
		for (x in 0..15) {
			for (z in 0..15) {
				val realBiome = EnumBiome.getBiome(chunk.getBiomeId(x, z))
				if (realBiome is CoveredBiome) {
					val biome = realBiome
					//just in case!
					synchronized(biome.synchronizeCover) {
						biome.preCover(realX or x, realZ or z)
						val coverBlock = biome.coverBlock shl 4
						var hasCovered = false
						var realY: Int
						//start one below build limit in case of cover blocks
						var y = 254
						while (y > 32) {
							if (chunk.getFullBlock(x, y, z) == STONE) {
								COVER@ if (!hasCovered) {
									if (y >= Normal.seaHeight) {
										chunk.setFullBlockId(x, y + 1, z, coverBlock)
										val surfaceDepth = biome.getSurfaceDepth(y)
										for (i in 0 until surfaceDepth) {
											realY = y - i
											if (chunk.getFullBlock(x, realY, z) == STONE) {
												chunk.setFullBlockId(x, realY, z, biome.getSurfaceBlock(realY) shl 4 or biome.getSurfaceMeta(realY))
											} else break@COVER
										}
										y -= surfaceDepth
									}
									val groundDepth = biome.getGroundDepth(y)
									for (i in 0 until groundDepth) {
										realY = y - i
										if (chunk.getFullBlock(x, realY, z) == STONE) {
											chunk.setFullBlockId(x, realY, z, biome.getGroundBlock(realY) shl 4 or biome.getGroundMeta(realY))
										} else break@COVER
									}
									//don't take all of groundDepth away because we do y-- in the loop
									y -= groundDepth - 1
								}
								hasCovered = true
							} else {
								if (hasCovered) {
									//reset it if this isn't a valid stone block (allows us to place ground cover on top and below overhangs)
									hasCovered = false
								}
							}
							y--
						}
					}
				}
			}
		}
	}

	companion object {
		const val STONE = BlockID.STONE shl 4
	}
}