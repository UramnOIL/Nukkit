package cn.nukkit.level.generator.populator.impl

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.level.generator.populator.helper.EnsureBelow
import cn.nukkit.level.generator.populator.helper.EnsureCover
import cn.nukkit.level.generator.populator.type.PopulatorSurfaceBlock
import cn.nukkit.math.NukkitRandom

/**
 * @author DaPorkchop_
 */
class PopulatorGroundFire : PopulatorSurfaceBlock() {
	override fun canStay(x: Int, y: Int, z: Int, chunk: FullChunk): Boolean {
		return EnsureCover.Companion.ensureCover(x, y, z, chunk) && EnsureBelow.Companion.ensureBelow(x, y, z, BlockID.NETHERRACK, chunk)
	}

	override fun getBlockId(x: Int, z: Int, random: NukkitRandom?, chunk: FullChunk?): Int {
		return BlockID.FIRE shl 4
	}

	override fun placeBlock(x: Int, y: Int, z: Int, id: Int, chunk: FullChunk, random: NukkitRandom?) {
		super.placeBlock(x, y, z, id, chunk, random)
		chunk.setBlockLight(x, y, z, Block.light!![BlockID.FIRE])
	}

	override fun getHighestWorkableBlock(level: ChunkManager?, x: Int, z: Int, chunk: FullChunk): Int {
		var y: Int
		y = 0
		while (y <= 127) {
			val b = chunk.getBlockId(x, y, z)
			if (b == Block.AIR) {
				break
			}
			++y
		}
		return if (y == 0) -1 else y
	}
}