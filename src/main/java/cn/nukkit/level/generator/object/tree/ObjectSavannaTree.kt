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

class ObjectSavannaTree : TreeGenerator() {
	override fun generate(level: ChunkManager, rand: NukkitRandom, position: Vector3?): Boolean {
		val i = rand.nextBoundedInt(3) + rand.nextBoundedInt(3) + 5
		var flag = true
		return if (position.getY() >= 1 && position.getY() + i + 1 <= 256) {
			for (j in position.getY() as Int..position.getY() + 1 + i) {
				var k = 1
				if (j == position.getY()) {
					k = 0
				}
				if (j >= position.getY() + 1 + i - 2) {
					k = 2
				}
				val vector3 = Vector3()
				var l = position.getX() as Int - k
				while (l <= position.getX() + k && flag) {
					var i1 = position.getZ() as Int - k
					while (i1 <= position.getZ() + k && flag) {
						if (j >= 0 && j < 256) {
							vector3.setComponents(l.toDouble(), j.toDouble(), i1.toDouble())
							if (!canGrowInto(level.getBlockIdAt(vector3.x as Int, vector3.y as Int, vector3.z as Int))) {
								flag = false
							}
						} else {
							flag = false
						}
						++i1
					}
					++l
				}
			}
			if (!flag) {
				false
			} else {
				val down = position!!.down()
				val block = level.getBlockIdAt(down!!.floorX, down.floorY, down.floorZ)
				if ((block == Block.GRASS || block == Block.DIRT) && position.getY() < 256 - i - 1) {
					this.setDirtAt(level, position.down()!!)
					val face = BlockFace.Plane.HORIZONTAL.random(rand)
					val k2 = i - rand.nextBoundedInt(4) - 1
					var l2 = 3 - rand.nextBoundedInt(3)
					var i3 = position.floorX
					var j1 = position.floorZ
					var k1 = 0
					for (l1 in 0 until i) {
						val i2 = position.floorY + l1
						if (l1 >= k2 && l2 > 0) {
							i3 += face.xOffset
							j1 += face.zOffset
							--l2
						}
						val blockpos = Vector3(i3.toDouble(), i2.toDouble(), j1.toDouble())
						val material = level.getBlockIdAt(blockpos.floorX, blockpos.floorY, blockpos.floorZ)
						if (material == Block.AIR || material == Block.LEAVES) {
							placeLogAt(level, blockpos)
							k1 = i2
						}
					}
					var blockpos2: Vector3? = Vector3(i3.toDouble(), k1.toDouble(), j1.toDouble())
					for (j3 in -3..3) {
						for (i4 in -3..3) {
							if (Math.abs(j3) != 3 || Math.abs(i4) != 3) {
								placeLeafAt(level, blockpos2!!.add(j3.toDouble(), 0.0, i4.toDouble()))
							}
						}
					}
					blockpos2 = blockpos2!!.up()
					for (k3 in -1..1) {
						for (j4 in -1..1) {
							placeLeafAt(level, blockpos2!!.add(k3.toDouble(), 0.0, j4.toDouble()))
						}
					}
					placeLeafAt(level, blockpos2!!.east(2))
					placeLeafAt(level, blockpos2.west(2))
					placeLeafAt(level, blockpos2.south(2))
					placeLeafAt(level, blockpos2.north(2))
					i3 = position.floorX
					j1 = position.floorZ
					val face1 = BlockFace.Plane.HORIZONTAL.random(rand)
					if (face1 !== face) {
						val l3 = k2 - rand.nextBoundedInt(2) - 1
						var k4 = 1 + rand.nextBoundedInt(3)
						k1 = 0
						var l4 = l3
						while (l4 < i && k4 > 0) {
							if (l4 >= 1) {
								val j2 = position.floorY + l4
								i3 += face1.xOffset
								j1 += face1.zOffset
								val blockpos1 = Vector3(i3.toDouble(), j2.toDouble(), j1.toDouble())
								val material1 = level.getBlockIdAt(blockpos1.floorX, blockpos1.floorY, blockpos1.floorZ)
								if (material1 == Block.AIR || material1 == Block.LEAVES) {
									placeLogAt(level, blockpos1)
									k1 = j2
								}
							}
							++l4
							--k4
						}
						if (k1 > 0) {
							var blockpos3: Vector3? = Vector3(i3.toDouble(), k1.toDouble(), j1.toDouble())
							for (i5 in -2..2) {
								for (k5 in -2..2) {
									if (Math.abs(i5) != 2 || Math.abs(k5) != 2) {
										placeLeafAt(level, blockpos3!!.add(i5.toDouble(), 0.0, k5.toDouble()))
									}
								}
							}
							blockpos3 = blockpos3!!.up()
							for (j5 in -1..1) {
								for (l5 in -1..1) {
									placeLeafAt(level, blockpos3!!.add(j5.toDouble(), 0.0, l5.toDouble()))
								}
							}
						}
					}
					true
				} else {
					false
				}
			}
		} else {
			false
		}
	}

	private fun placeLogAt(worldIn: ChunkManager, pos: Vector3) {
		this.setBlockAndNotifyAdequately(worldIn, pos, TRUNK)
	}

	private fun placeLeafAt(worldIn: ChunkManager, pos: Vector3?) {
		val material = worldIn.getBlockIdAt(pos!!.floorX, pos.floorY, pos.floorZ)
		if (material == Block.AIR || material == Block.LEAVES) {
			this.setBlockAndNotifyAdequately(worldIn, pos, LEAF)
		}
	}

	companion object {
		private val TRUNK = get(BlockID.WOOD2, BlockWood2.ACACIA)
		private val LEAF = get(BlockID.LEAVES2, BlockLeaves2.ACACIA)
	}
}