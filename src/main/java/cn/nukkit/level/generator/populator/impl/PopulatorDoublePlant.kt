package cn.nukkit.level.generator.populator.impl

import cn.nukkit.block.BlockID
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.level.generator.populator.helper.EnsureCover
import cn.nukkit.level.generator.populator.helper.EnsureGrassBelow
import cn.nukkit.level.generator.populator.type.PopulatorSurfaceBlock
import cn.nukkit.math.NukkitRandom

/**
 * author: DaPorkchop_
 * Nukkit Project
 */
class PopulatorDoublePlant(private val type: Int) : PopulatorSurfaceBlock() {
	override fun canStay(x: Int, y: Int, z: Int, chunk: FullChunk): Boolean {
		return EnsureCover.Companion.ensureCover(x, y, z, chunk) && EnsureCover.Companion.ensureCover(x, y + 1, z, chunk) && EnsureGrassBelow.Companion.ensureGrassBelow(x, y, z, chunk)
	}

	override fun getBlockId(x: Int, z: Int, random: NukkitRandom?, chunk: FullChunk?): Int {
		return BlockID.DOUBLE_PLANT shl 4 or type
	}

	override fun placeBlock(x: Int, y: Int, z: Int, id: Int, chunk: FullChunk, random: NukkitRandom?) {
		super.placeBlock(x, y, z, id, chunk, random)
		chunk.setFullBlockId(x, y + 1, z, 8 or id)
	}

}