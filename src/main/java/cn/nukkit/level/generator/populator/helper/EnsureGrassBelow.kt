package cn.nukkit.level.generator.populator.helper

import cn.nukkit.block.BlockID
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator

/**
 * @author DaPorkchop_
 */
interface EnsureGrassBelow {
	companion object {
		fun ensureGrassBelow(x: Int, y: Int, z: Int, chunk: FullChunk): Boolean {
			return EnsureBelow.Companion.ensureBelow(x, y, z, BlockID.GRASS, chunk)
		}
	}
}