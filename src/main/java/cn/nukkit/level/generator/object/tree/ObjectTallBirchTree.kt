package cn.nukkit.level.generator.`object`.tree

import cn.nukkit.level.ChunkManager
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ObjectTallBirchTree : ObjectBirchTree() {
	override fun placeObject(level: ChunkManager?, x: Int, y: Int, z: Int, random: NukkitRandom) {
		treeHeight = random.nextBoundedInt(3) + 10
		super.placeObject(level, x, y, z, random)
	}
}