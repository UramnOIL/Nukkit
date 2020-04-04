package cn.nukkit.level.generator.populator.impl

import cn.nukkit.block.BlockID
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.level.generator.populator.helper.PopulatorHelpers
import cn.nukkit.level.generator.populator.type.PopulatorSurfaceBlock
import cn.nukkit.math.NukkitRandom

/**
 * author: DaPorkchop_
 * Nukkit Project
 */
class PopulatorGrass : PopulatorSurfaceBlock() {
	override fun canStay(x: Int, y: Int, z: Int, chunk: FullChunk): Boolean {
		return PopulatorHelpers.canGrassStay(x, y, z, chunk)
	}

	override fun getBlockId(x: Int, z: Int, random: NukkitRandom?, chunk: FullChunk?): Int {
		return BlockID.TALL_GRASS shl 4 or 1
	}
}