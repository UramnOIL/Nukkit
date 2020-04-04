package cn.nukkit.level.generator.`object`.mushroom

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.generator
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3

class BigMushroom : BasicGenerator {
	/**
	 * The mushroom type. 0 for brown, 1 for red.
	 */
	private var mushroomType: Int

	constructor(mushroomType: Int) {
		this.mushroomType = mushroomType
	}

	constructor() {
		mushroomType = -1
	}

	override fun generate(level: ChunkManager, rand: NukkitRandom, position: Vector3?): Boolean {
		var block = mushroomType
		if (block < 0) {
			block = if (rand.nextBoolean()) RED else BROWN
		}
		val mushroom = if (block == 0) get(BlockID.BROWN_MUSHROOM_BLOCK) else get(BlockID.RED_MUSHROOM_BLOCK)
		var i = rand.nextBoundedInt(3) + 4
		if (rand.nextBoundedInt(12) == 0) {
			i *= 2
		}
		var flag = true
		return if (position.getY() >= 1 && position.getY() + i + 1 < 256) {
			for (j in position!!.floorY..position.getY() + 1 + i) {
				var k = 3
				if (j <= position.getY() + 3) {
					k = 0
				}
				val pos = Vector3()
				var l = position.floorX - k
				while (l <= position.getX() + k && flag) {
					var i1 = position.floorZ - k
					while (i1 <= position.getZ() + k && flag) {
						if (j >= 0 && j < 256) {
							pos.setComponents(l.toDouble(), j.toDouble(), i1.toDouble())
							val material = level.getBlockIdAt(pos.floorX, pos.floorY, pos.floorZ)
							if (material != Block.AIR && material != Block.LEAVES) {
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
				val pos2 = position.down()
				val block1 = level.getBlockIdAt(pos2!!.floorX, pos2.floorY, pos2.floorZ)
				if (block1 != Block.DIRT && block1 != Block.GRASS && block1 != Block.MYCELIUM) {
					false
				} else {
					var k2 = position.floorY + i
					if (block == RED) {
						k2 = position.floorY + i - 3
					}
					for (l2 in k2..position.getY() + i) {
						var j3 = 1
						if (l2 < position.getY() + i) {
							++j3
						}
						if (block == BROWN) {
							j3 = 3
						}
						val k3 = position.floorX - j3
						val l3 = position.floorX + j3
						val j1 = position.floorZ - j3
						val k1 = position.floorZ + j3
						for (l1 in k3..l3) {
							for (i2 in j1..k1) {
								var j2 = 5
								if (l1 == k3) {
									--j2
								} else if (l1 == l3) {
									++j2
								}
								if (i2 == j1) {
									j2 -= 3
								} else if (i2 == k1) {
									j2 += 3
								}
								var meta = j2
								if (block == BROWN || l2 < position.getY() + i) {
									if ((l1 == k3 || l1 == l3) && (i2 == j1 || i2 == k1)) {
										continue
									}
									if (l1 == position.getX() - (j3 - 1) && i2 == j1) {
										meta = NORTH_WEST
									}
									if (l1 == k3 && i2 == position.getZ() - (j3 - 1)) {
										meta = NORTH_WEST
									}
									if (l1 == position.getX() + (j3 - 1) && i2 == j1) {
										meta = NORTH_EAST
									}
									if (l1 == l3 && i2 == position.getZ() - (j3 - 1)) {
										meta = NORTH_EAST
									}
									if (l1 == position.getX() - (j3 - 1) && i2 == k1) {
										meta = SOUTH_WEST
									}
									if (l1 == k3 && i2 == position.getZ() + (j3 - 1)) {
										meta = SOUTH_WEST
									}
									if (l1 == position.getX() + (j3 - 1) && i2 == k1) {
										meta = SOUTH_EAST
									}
									if (l1 == l3 && i2 == position.getZ() + (j3 - 1)) {
										meta = SOUTH_EAST
									}
								}
								if (meta == CENTER && l2 < position.getY() + i) {
									meta = ALL_INSIDE
								}
								if (position.getY() >= position.getY() + i - 1 || meta != ALL_INSIDE) {
									val blockPos = Vector3(l1.toDouble(), l2.toDouble(), i2.toDouble())
									if (!Block.solid!![level.getBlockIdAt(blockPos.floorX, blockPos.floorY, blockPos.floorZ)]) {
										mushroom.damage = meta
										this.setBlockAndNotifyAdequately(level, blockPos, mushroom)
									}
								}
							}
						}
					}
					for (i3 in 0 until i) {
						val pos = position.up(i3)
						val id = level.getBlockIdAt(pos!!.floorX, pos.floorY, pos.floorZ)
						if (!Block.solid!![id]) {
							mushroom.damage = STEM
							this.setBlockAndNotifyAdequately(level, pos, mushroom)
						}
					}
					true
				}
			}
		} else {
			false
		}
	}

	companion object {
		const val NORTH_WEST = 1
		const val NORTH = 2
		const val NORTH_EAST = 3
		const val WEST = 4
		const val CENTER = 5
		const val EAST = 6
		const val SOUTH_WEST = 7
		const val SOUTH = 8
		const val SOUTH_EAST = 9
		const val STEM = 10
		const val ALL_INSIDE = 0
		const val ALL_OUTSIDE = 14
		const val ALL_STEM = 15
		const val BROWN = 0
		const val RED = 1
	}
}