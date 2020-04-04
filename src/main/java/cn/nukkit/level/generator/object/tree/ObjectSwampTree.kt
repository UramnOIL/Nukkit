package cn.nukkit.level.generator.`object`.tree

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.block.BlockLeaves
import cn.nukkit.block.BlockWood
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.generator
import cn.nukkit.math.BlockVector3
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3

class ObjectSwampTree : TreeGenerator() {
	/**
	 * The metadata value of the wood to use in tree generation.
	 */
	private val metaWood = get(BlockID.WOOD, BlockWood.OAK)

	/**
	 * The metadata value of the leaves to use in tree generation.
	 */
	private val metaLeaves = get(BlockID.LEAVES, BlockLeaves.OAK)
	override fun generate(worldIn: ChunkManager, rand: NukkitRandom, vectorPosition: Vector3?): Boolean {
		val position = BlockVector3(vectorPosition!!.floorX, vectorPosition.floorY, vectorPosition.floorZ)
		val i = rand.nextBoundedInt(4) + 5
		var flag = true
		return if (position.y >= 1 && position.y + i + 1 <= 256) {
			for (j in position.y..position.y + 1 + i) {
				var k = 1
				if (j == position.y) {
					k = 0
				}
				if (j >= position.y + 1 + i - 2) {
					k = 3
				}
				val pos2 = BlockVector3()
				var l = position.x - k
				while (l <= position.x + k && flag) {
					var i1 = position.z - k
					while (i1 <= position.z + k && flag) {
						if (j >= 0 && j < 256) {
							pos2.setComponents(l, j, i1)
							if (!canGrowInto(worldIn.getBlockIdAt(pos2.x, pos2.y, pos2.z))) {
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
				val down = position.down()
				val block = worldIn.getBlockIdAt(down.x, down.y, down.z)
				if ((block == Block.GRASS || block == Block.DIRT) && position.y < 256 - i - 1) {
					this.setDirtAt(worldIn, down)
					for (k1 in position.y - 3 + i..position.y + i) {
						val j2 = k1 - (position.y + i)
						val l2 = 2 - j2 / 2
						for (j3 in position.x - l2..position.x + l2) {
							val k3 = j3 - position.x
							for (i4 in position.z - l2..position.z + l2) {
								val j1 = i4 - position.z
								if (Math.abs(k3) != l2 || Math.abs(j1) != l2 || rand.nextBoundedInt(2) != 0 && j2 != 0) {
									val blockpos = BlockVector3(j3, k1, i4)
									val id = worldIn.getBlockIdAt(blockpos.x, blockpos.y, blockpos.z)
									if (id == Block.AIR || id == Block.LEAVES || id == Block.VINE) {
										this.setBlockAndNotifyAdequately(worldIn, blockpos, metaLeaves)
									}
								}
							}
						}
					}
					for (l1 in 0 until i) {
						val up = position.up(l1)
						val id = worldIn.getBlockIdAt(up.x, up.y, up.z)
						if (id == Block.AIR || id == Block.LEAVES || id == Block.WATER || id == Block.STILL_WATER) {
							this.setBlockAndNotifyAdequately(worldIn, up, metaWood)
						}
					}
					for (i2 in position.y - 3 + i..position.y + i) {
						val k2 = i2 - (position.y + i)
						val i3 = 2 - k2 / 2
						val pos2 = BlockVector3()
						for (l3 in position.x - i3..position.x + i3) {
							for (j4 in position.z - i3..position.z + i3) {
								pos2.setComponents(l3, i2, j4)
								if (worldIn.getBlockIdAt(pos2.x, pos2.y, pos2.z) == Block.LEAVES) {
									val blockpos2 = pos2.west()
									val blockpos3 = pos2.east()
									val blockpos4 = pos2.north()
									val blockpos1 = pos2.south()
									if (rand.nextBoundedInt(4) == 0 && worldIn.getBlockIdAt(blockpos2.x, blockpos2.y, blockpos2.z) == Block.AIR) {
										addHangingVine(worldIn, blockpos2, 8)
									}
									if (rand.nextBoundedInt(4) == 0 && worldIn.getBlockIdAt(blockpos3.x, blockpos3.y, blockpos3.z) == Block.AIR) {
										addHangingVine(worldIn, blockpos3, 2)
									}
									if (rand.nextBoundedInt(4) == 0 && worldIn.getBlockIdAt(blockpos4.x, blockpos4.y, blockpos4.z) == Block.AIR) {
										addHangingVine(worldIn, blockpos4, 1)
									}
									if (rand.nextBoundedInt(4) == 0 && worldIn.getBlockIdAt(blockpos1.x, blockpos1.y, blockpos1.z) == Block.AIR) {
										addHangingVine(worldIn, blockpos1, 4)
									}
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

	private fun addVine(worldIn: ChunkManager, pos: BlockVector3, meta: Int) {
		this.setBlockAndNotifyAdequately(worldIn, pos, get(BlockID.VINE, meta))
	}

	private fun addHangingVine(worldIn: ChunkManager, pos: BlockVector3, meta: Int) {
		var pos = pos
		addVine(worldIn, pos, meta)
		var i = 4
		pos = pos.down()
		while (i > 0 && worldIn.getBlockIdAt(pos.x, pos.y, pos.z) == Block.AIR) {
			addVine(worldIn, pos, meta)
			pos = pos.down()
			--i
		}
	}
}