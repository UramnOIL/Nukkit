package cn.nukkit.level.generator.`object`.tree

import cn.nukkit.block.Block
import cn.nukkit.block.BlockWood
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class ObjectSpruceTree : ObjectTree() {
	override var treeHeight = 0
		protected set
	override val trunkBlock: Int
		get() = Block.LOG

	override val leafBlock: Int
		get() = Block.LEAVES

	override val type: Int
		get() = BlockWood.SPRUCE

	override fun placeObject(level: ChunkManager?, x: Int, y: Int, z: Int, random: NukkitRandom) {
		treeHeight = random.nextBoundedInt(4) + 6
		val topSize = treeHeight - (1 + random.nextBoundedInt(2))
		val lRadius = 2 + random.nextBoundedInt(2)
		placeTrunk(level, x, y, z, random, treeHeight - random.nextBoundedInt(3))
		placeLeaves(level, topSize, lRadius, x, y, z, random)
	}

	fun placeLeaves(level: ChunkManager?, topSize: Int, lRadius: Int, x: Int, y: Int, z: Int, random: NukkitRandom) {
		var radius = random.nextBoundedInt(2)
		var maxR = 1
		var minR = 0
		for (yy in 0..topSize) {
			val yyy = y + treeHeight - yy
			for (xx in x - radius..x + radius) {
				val xOff = Math.abs(xx - x)
				for (zz in z - radius..z + radius) {
					val zOff = Math.abs(zz - z)
					if (xOff == radius && zOff == radius && radius > 0) {
						continue
					}
					if (!Block.solid!![level!!.getBlockIdAt(xx, yyy, zz)]) {
						level.setBlockAt(xx, yyy, zz, leafBlock, type)
					}
				}
			}
			if (radius >= maxR) {
				radius = minR
				minR = 1
				if (++maxR > lRadius) {
					maxR = lRadius
				}
			} else {
				++radius
			}
		}
	}
}