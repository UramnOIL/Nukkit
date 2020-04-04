package cn.nukkit.level.generator.`object`.tree

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.block.BlockLeaves2
import cn.nukkit.block.BlockWood2
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.generator
import cn.nukkit.math.BlockFace
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3

/**
 * Created by CreeperFace on 23. 10. 2016.
 */
class ObjectDarkOakTree : TreeGenerator() {
	override fun generate(level: ChunkManager, rand: NukkitRandom, position: Vector3?): Boolean {
		val i = rand.nextBoundedInt(3) + rand.nextBoundedInt(2) + 6
		val j = position!!.floorX
		val k = position.floorY
		val l = position.floorZ
		return if (k >= 1 && k + i + 1 < 256) {
			val blockpos = position.down()
			val block = level.getBlockIdAt(blockpos!!.floorX, blockpos.floorY, blockpos.floorZ)
			if (block != Block.GRASS && block != Block.DIRT) {
				false
			} else if (!placeTreeOfHeight(level, position, i)) {
				false
			} else {
				this.setDirtAt(level, blockpos)
				this.setDirtAt(level, blockpos.east()!!)
				this.setDirtAt(level, blockpos.south()!!)
				this.setDirtAt(level, blockpos.south()!!.east()!!)
				val enumfacing = BlockFace.Plane.HORIZONTAL.random(rand)
				val i1 = i - rand.nextBoundedInt(4)
				var j1 = 2 - rand.nextBoundedInt(3)
				var k1 = j
				var l1 = l
				val i2 = k + i - 1
				for (j2 in 0 until i) {
					if (j2 >= i1 && j1 > 0) {
						k1 += enumfacing.xOffset
						l1 += enumfacing.zOffset
						--j1
					}
					val k2 = k + j2
					val blockpos1 = Vector3(k1.toDouble(), k2.toDouble(), l1.toDouble())
					val material = level.getBlockIdAt(blockpos1.floorX, blockpos1.floorY, blockpos1.floorZ)
					if (material == Block.AIR || material == Block.LEAVES) {
						placeLogAt(level, blockpos1)
						placeLogAt(level, blockpos1.east())
						placeLogAt(level, blockpos1.south())
						placeLogAt(level, blockpos1.east()!!.south())
					}
				}
				for (i3 in -2..0) {
					for (l3 in -2..0) {
						var k4 = -1
						placeLeafAt(level, k1 + i3, i2 + k4, l1 + l3)
						placeLeafAt(level, 1 + k1 - i3, i2 + k4, l1 + l3)
						placeLeafAt(level, k1 + i3, i2 + k4, 1 + l1 - l3)
						placeLeafAt(level, 1 + k1 - i3, i2 + k4, 1 + l1 - l3)
						if ((i3 > -2 || l3 > -1) && (i3 != -1 || l3 != -2)) {
							k4 = 1
							placeLeafAt(level, k1 + i3, i2 + k4, l1 + l3)
							placeLeafAt(level, 1 + k1 - i3, i2 + k4, l1 + l3)
							placeLeafAt(level, k1 + i3, i2 + k4, 1 + l1 - l3)
							placeLeafAt(level, 1 + k1 - i3, i2 + k4, 1 + l1 - l3)
						}
					}
				}
				if (rand.nextBoolean()) {
					placeLeafAt(level, k1, i2 + 2, l1)
					placeLeafAt(level, k1 + 1, i2 + 2, l1)
					placeLeafAt(level, k1 + 1, i2 + 2, l1 + 1)
					placeLeafAt(level, k1, i2 + 2, l1 + 1)
				}
				for (j3 in -3..4) {
					for (i4 in -3..4) {
						if ((j3 != -3 || i4 != -3) && (j3 != -3 || i4 != 4) && (j3 != 4 || i4 != -3) && (j3 != 4 || i4 != 4) && (Math.abs(j3) < 3 || Math.abs(i4) < 3)) {
							placeLeafAt(level, k1 + j3, i2, l1 + i4)
						}
					}
				}
				for (k3 in -1..2) {
					for (j4 in -1..2) {
						if ((k3 < 0 || k3 > 1 || j4 < 0 || j4 > 1) && rand.nextBoundedInt(3) <= 0) {
							val l4 = rand.nextBoundedInt(3) + 2
							for (i5 in 0 until l4) {
								placeLogAt(level, Vector3((j + k3).toDouble(), (i2 - i5 - 1).toDouble(), (l + j4).toDouble()))
							}
							for (j5 in -1..1) {
								for (l2 in -1..1) {
									placeLeafAt(level, k1 + k3 + j5, i2, l1 + j4 + l2)
								}
							}
							for (k5 in -2..2) {
								for (l5 in -2..2) {
									if (Math.abs(k5) != 2 || Math.abs(l5) != 2) {
										placeLeafAt(level, k1 + k3 + k5, i2 - 1, l1 + j4 + l5)
									}
								}
							}
						}
					}
				}
				true
			}
		} else {
			false
		}
	}

	private fun placeTreeOfHeight(worldIn: ChunkManager, pos: Vector3?, height: Int): Boolean {
		val i = pos!!.floorX
		val j = pos.floorY
		val k = pos.floorZ
		val blockPos = Vector3()
		for (l in 0..height + 1) {
			var i1 = 1
			if (l == 0) {
				i1 = 0
			}
			if (l >= height - 1) {
				i1 = 2
			}
			for (j1 in -i1..i1) {
				for (k1 in -i1..i1) {
					blockPos.setComponents(i + j1.toDouble(), j + l.toDouble(), k + k1.toDouble())
					if (!canGrowInto(worldIn.getBlockIdAt(blockPos.floorX, blockPos.floorY, blockPos.floorZ))) {
						return false
					}
				}
			}
		}
		return true
	}

	private fun placeLogAt(worldIn: ChunkManager, pos: Vector3?) {
		if (canGrowInto(worldIn.getBlockIdAt(pos!!.floorX, pos.floorY, pos.floorZ))) {
			this.setBlockAndNotifyAdequately(worldIn, pos, DARK_OAK_LOG)
		}
	}

	private fun placeLeafAt(worldIn: ChunkManager, x: Int, y: Int, z: Int) {
		val blockpos = Vector3(x.toDouble(), y.toDouble(), z.toDouble())
		val material = worldIn.getBlockIdAt(blockpos.floorX, blockpos.floorY, blockpos.floorZ)
		if (material == Block.AIR) {
			this.setBlockAndNotifyAdequately(worldIn, blockpos, DARK_OAK_LEAVES)
		}
	}

	companion object {
		private val DARK_OAK_LOG = get(BlockID.WOOD2, BlockWood2.DARK_OAK)
		private val DARK_OAK_LEAVES = get(BlockID.LEAVES2, BlockLeaves2.DARK_OAK)
	}
}