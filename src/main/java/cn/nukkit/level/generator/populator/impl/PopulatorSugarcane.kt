package cn.nukkit.level.generator.populator.impl

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID
import cn.nukkit.level.Level
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator.populator.helper.EnsureBelow
import cn.nukkit.level.generator.populator.helper.EnsureCover
import cn.nukkit.level.generator.populator.helper.EnsureGrassBelow
import cn.nukkit.level.generator.populator.type.PopulatorSurfaceBlock
import cn.nukkit.math.NukkitRandom

/**
 * @author Niall Lindsay (Niall7459)
 *
 *
 * Nukkit Project
 *
 */
open class PopulatorSugarcane : PopulatorSurfaceBlock() {
	private fun findWater(x: Int, y: Int, z: Int, level: Level?): Boolean {
		var count = 0
		for (i in x - 4 until x + 4) {
			for (j in z - 4 until z + 4) {
				val b = level!!.getBlockIdAt(i, y, j)
				if (b == Block.WATER || b == Block.STILL_WATER) {
					count++
				}
				if (count > 10) {
					return true
				}
			}
		}
		return count > 10
	}

	override fun canStay(x: Int, y: Int, z: Int, chunk: FullChunk): Boolean {
		return EnsureCover.Companion.ensureCover(x, y, z, chunk) && (EnsureGrassBelow.Companion.ensureGrassBelow(x, y, z, chunk) || EnsureBelow.Companion.ensureBelow(x, y, z, BlockID.SAND, chunk)) && findWater(x, y - 1, z, chunk.provider!!.level)
	}

	override fun getBlockId(x: Int, z: Int, random: NukkitRandom?, chunk: FullChunk?): Int {
		return BlockID.SUGARCANE_BLOCK shl 4 or 1
	}
}