package cn.nukkit.level.generator.`object`.tree

import cn.nukkit.block.Block
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom

/**
 * author: DaPorkchop_
 * Nukkit Project
 */
class ObjectBigSpruceTree(private val leafStartHeightMultiplier: Float, private val baseLeafRadius: Int) : ObjectSpruceTree() {
	override fun placeObject(level: ChunkManager?, x: Int, y: Int, z: Int, random: NukkitRandom) {
		treeHeight = random.nextBoundedInt(15) + 20
		val topSize = treeHeight - (treeHeight * leafStartHeightMultiplier).toInt()
		val lRadius = baseLeafRadius + random.nextBoundedInt(2)
		placeTrunk(level, x, y, z, random, getTreeHeight() - random.nextBoundedInt(3))
		placeLeaves(level, topSize, lRadius, x, y, z, random)
	}

	override fun placeTrunk(level: ChunkManager?, x: Int, y: Int, z: Int, random: NukkitRandom?, trunkHeight: Int) {
		// The base dirt block
		level!!.setBlockAt(x, y - 1, z, Block.DIRT)
		val radius = 2
		for (yy in 0 until trunkHeight) {
			for (xx in 0 until radius) {
				for (zz in 0 until radius) {
					val blockId = level.getBlockIdAt(x, y + yy, z)
					if (overridable(blockId)) {
						level.setBlockAt(x + xx, y + yy, z + zz, this.trunkBlock, this.type)
					}
				}
			}
		}
	}

}