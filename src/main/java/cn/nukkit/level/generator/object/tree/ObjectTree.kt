package cn.nukkit.level.generator.`object`.tree

import cn.nukkit.block.Block
import cn.nukkit.block.BlockSapling
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class ObjectTree {
	protected fun overridable(id: Int): Boolean {
		return when (id) {
			Block.AIR, Block.SAPLING, Block.LOG, Block.LEAVES, Block.SNOW_LAYER, Block.LOG2, Block.LEAVES2 -> true
			else -> false
		}
	}

	open val type: Int
		get() = 0

	open val trunkBlock: Int
		get() = Block.LOG

	open val leafBlock: Int
		get() = Block.LEAVES

	open val treeHeight: Int
		get() = 7

	fun canPlaceObject(level: ChunkManager?, x: Int, y: Int, z: Int, random: NukkitRandom?): Boolean {
		var radiusToCheck = 0
		for (yy in 0 until treeHeight + 3) {
			if (yy == 1 || yy == treeHeight) {
				++radiusToCheck
			}
			for (xx in -radiusToCheck until radiusToCheck + 1) {
				for (zz in -radiusToCheck until radiusToCheck + 1) {
					if (!overridable(level!!.getBlockIdAt(x + xx, y + yy, z + zz))) {
						return false
					}
				}
			}
		}
		return true
	}

	open fun placeObject(level: ChunkManager?, x: Int, y: Int, z: Int, random: NukkitRandom) {
		placeTrunk(level, x, y, z, random, treeHeight - 1)
		for (yy in y - 3 + treeHeight..y + treeHeight) {
			val yOff = yy - (y + treeHeight).toDouble()
			val mid = (1 - yOff / 2).toInt()
			for (xx in x - mid..x + mid) {
				val xOff = Math.abs(xx - x)
				for (zz in z - mid..z + mid) {
					val zOff = Math.abs(zz - z)
					if (xOff == mid && zOff == mid && (yOff == 0.0 || random.nextBoundedInt(2) == 0)) {
						continue
					}
					if (!Block.solid!![level!!.getBlockIdAt(xx, yy, zz)]) {
						level.setBlockAt(xx, yy, zz, leafBlock, type)
					}
				}
			}
		}
	}

	protected open fun placeTrunk(level: ChunkManager?, x: Int, y: Int, z: Int, random: NukkitRandom?, trunkHeight: Int) {
		// The base dirt block
		level!!.setBlockAt(x, y - 1, z, Block.DIRT)
		for (yy in 0 until trunkHeight) {
			val blockId = level.getBlockIdAt(x, y + yy, z)
			if (overridable(blockId)) {
				level.setBlockAt(x, y + yy, z, trunkBlock, type)
			}
		}
	}

	companion object {
		@JvmOverloads
		fun growTree(level: ChunkManager?, x: Int, y: Int, z: Int, random: NukkitRandom, type: Int = 0) {
			val tree: ObjectTree
			tree = when (type) {
				BlockSapling.SPRUCE -> ObjectSpruceTree()
				BlockSapling.BIRCH -> ObjectBirchTree()
				BlockSapling.JUNGLE -> ObjectJungleTree()
				BlockSapling.BIRCH_TALL -> ObjectTallBirchTree()
				BlockSapling.OAK -> ObjectOakTree()
				else -> ObjectOakTree()
			}
			if (tree.canPlaceObject(level, x, y, z, random)) {
				tree.placeObject(level, x, y, z, random)
			}
		}
	}
}