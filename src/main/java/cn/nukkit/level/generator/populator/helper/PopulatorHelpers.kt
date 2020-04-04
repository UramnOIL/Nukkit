package cn.nukkit.level.generator.populator.helper

import cn.nukkit.block.BlockID
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet

/**
 * @author DaPorkchop_
 */
object PopulatorHelpers : BlockID {
	private val nonSolidBlocks: IntSet = IntOpenHashSet()
	fun canGrassStay(x: Int, y: Int, z: Int, chunk: FullChunk): Boolean {
		return EnsureCover.Companion.ensureCover(x, y, z, chunk) && EnsureGrassBelow.Companion.ensureGrassBelow(x, y, z, chunk)
	}

	fun isNonSolid(id: Int): Boolean {
		return nonSolidBlocks.contains(id)
	}

	init {
		nonSolidBlocks.add(BlockID.AIR)
		nonSolidBlocks.add(BlockID.LEAVES)
		nonSolidBlocks.add(BlockID.LEAVES2)
		nonSolidBlocks.add(BlockID.SNOW_LAYER)
	}
}