package cn.nukkit.level.generator.populator.impl

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.level.generator.populator.type.Populator
import cn.nukkit.math.NukkitMath.randomRange
import cn.nukkit.math.NukkitRandom

class PopulatorGlowStone : Populator() {
	override fun populate(level: ChunkManager, chunkX: Int, chunkZ: Int, random: NukkitRandom, chunk: FullChunk) {
		val x = randomRange(random, chunkX shl 4, (chunkX shl 4) + 15)
		val z = randomRange(random, chunkZ shl 4, (chunkZ shl 4) + 15)
		val y = this.getHighestWorkableBlock(chunk, x and 0xF, z and 0xF)
		if (y != -1 && level.getBlockIdAt(x, y, z) != BlockID.NETHERRACK) {
			val count = randomRange(random, 40, 60)
			for (i in 0 until count) {
				level.setBlockAt(x + (random.nextBoundedInt(7) - 3), y + (random.nextBoundedInt(9) - 4), z + (random.nextBoundedInt(7) - 3), BlockID.GLOWSTONE)
			}
		}
	}

	private fun getHighestWorkableBlock(chunk: FullChunk, x: Int, z: Int): Int {
		var y: Int
		//start scanning a bit lower down to allow space for placing on top
		y = 120
		while (y >= 0) {
			val b = chunk.getBlockId(x, y, z)
			if (b == Block.AIR) {
				break
			}
			y--
		}
		return if (y == 0) -1 else y
	}
}