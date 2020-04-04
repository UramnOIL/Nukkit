package cn.nukkit.level.generator.populator.impl

import cn.nukkit.block.BlockID
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.level.generator.populator.helper.EnsureBelow
import cn.nukkit.level.generator.populator.helper.EnsureCover
import cn.nukkit.level.generator.populator.type.PopulatorSurfaceBlock
import cn.nukkit.math.NukkitRandom

/**
 * @author DaPorkchop_
 */
class PopulatorCactus : PopulatorSurfaceBlock() {
	override fun canStay(x: Int, y: Int, z: Int, chunk: FullChunk): Boolean {
		return EnsureCover.Companion.ensureCover(x, y, z, chunk) && EnsureBelow.Companion.ensureBelow(x, y, z, BlockID.SAND, chunk)
	}

	override fun getBlockId(x: Int, z: Int, random: NukkitRandom?, chunk: FullChunk?): Int {
		return BlockID.CACTUS shl 4 or 1
	}
}