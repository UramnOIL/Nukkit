package cn.nukkit.level.generator.`object`

import cn.nukkit.block.Block
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3

/**
 * author: ItsLucas
 * Nukkit Project
 */
object ObjectTallGrass {
	fun growGrass(level: ChunkManager, pos: Vector3, random: NukkitRandom) {
		for (i in 0..127) {
			var num = 0
			var x = pos.floorX
			var y = pos.floorY + 1
			var z = pos.floorZ
			while (true) {
				if (num >= i / 16) {
					if (level.getBlockIdAt(x, y, z) == Block.AIR) {
						if (random.nextBoundedInt(8) == 0) {
							//porktodo: biomes have specific flower types that can grow in them
							if (random.nextBoolean()) {
								level.setBlockAt(x, y, z, Block.DANDELION)
							} else {
								level.setBlockAt(x, y, z, Block.POPPY)
							}
						} else {
							level.setBlockAt(x, y, z, Block.TALL_GRASS, 1)
						}
					}
					break
				}
				x += random.nextRange(-1, 1)
				y += random.nextRange(-1, 1) * random.nextBoundedInt(3) / 2
				z += random.nextRange(-1, 1)
				if (level.getBlockIdAt(x, y - 1, z) != Block.GRASS || y > 255 || y < 0) {
					break
				}
				++num
			}
		}
	}
}