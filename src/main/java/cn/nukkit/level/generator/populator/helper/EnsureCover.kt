package cn.nukkit.level.generator.populator.helper

import cn.nukkit.block.BlockID
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator

/**
 * @author DaPorkchop_
 */
interface EnsureCover {
	companion object {
		fun ensureCover(x: Int, y: Int, z: Int, chunk: FullChunk): Boolean {
			val id = chunk.getBlockId(x, y, z)
			return id == BlockID.AIR || id == BlockID.SNOW_LAYER
		}
	}
}