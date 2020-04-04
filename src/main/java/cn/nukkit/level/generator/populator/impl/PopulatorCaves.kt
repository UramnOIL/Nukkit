package cn.nukkit.level.generator.populator.impl

import cn.nukkit.block.Block
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.biome.EnumBiome
import cn.nukkit.level.biome.type.CoveredBiome
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.level.generator.populator.type.Populator
import cn.nukkit.math.MathHelper.cos
import cn.nukkit.math.MathHelper.floor
import cn.nukkit.math.MathHelper.sin
import cn.nukkit.math.NukkitRandom
import java.util.*

/**
 * author: Angelic47
 * Nukkit Project
 */
class PopulatorCaves : Populator() {
	protected var checkAreaSize = 8
	private var random: Random? = null
	var worldHeightCap = 128
	override fun populate(level: ChunkManager, chunkX: Int, chunkZ: Int, random: NukkitRandom, chunk: FullChunk) {
		this.random = Random()
		this.random!!.setSeed(level.seed)
		val worldLong1 = this.random!!.nextLong()
		val worldLong2 = this.random!!.nextLong()
		val size = checkAreaSize
		for (x in chunkX - size..chunkX + size) for (z in chunkZ - size..chunkZ + size) {
			val randomX = x * worldLong1
			val randomZ = z * worldLong2
			this.random!!.setSeed(randomX xor randomZ xor level.seed)
			generateChunk(x, z, chunk)
		}
	}

	protected fun generateLargeCaveNode(seed: Long, chunk: FullChunk, x: Double, y: Double, z: Double) {
		generateCaveNode(seed, chunk, x, y, z, 1.0f + random!!.nextFloat() * 6.0f, 0.0f, 0.0f, -1, -1, 0.5)
	}

	protected fun generateCaveNode(seed: Long, chunk: FullChunk, x: Double, y: Double, z: Double, radius: Float, angelOffset: Float, angel: Float, angle: Int, maxAngle: Int, scale: Double) {
		var x = x
		var y = y
		var z = z
		var angelOffset = angelOffset
		var angel = angel
		var angle = angle
		var maxAngle = maxAngle
		val chunkX = chunk.x
		val chunkZ = chunk.z
		val realX = chunkX * 16 + 8.toDouble()
		val realZ = chunkZ * 16 + 8.toDouble()
		var f1 = 0.0f
		var f2 = 0.0f
		val localRandom = Random(seed)
		if (maxAngle <= 0) {
			val checkAreaSize = checkAreaSize * 16 - 16
			maxAngle = checkAreaSize - localRandom.nextInt(checkAreaSize / 4)
		}
		var isLargeCave = false
		if (angle == -1) {
			angle = maxAngle / 2
			isLargeCave = true
		}
		val randomAngel = localRandom.nextInt(maxAngle / 2) + maxAngle / 4
		val bigAngel = localRandom.nextInt(6) == 0
		while (angle < maxAngle) {
			val offsetXZ = 1.5 + sin(angle * 3.141593f / maxAngle) * radius * 1.0f
			val offsetY = offsetXZ * scale
			val cos = cos(angel)
			val sin = sin(angel)
			x += cos(angelOffset) * cos.toDouble()
			y += sin.toDouble()
			z += sin(angelOffset) * cos.toDouble()
			angel *= if (bigAngel) 0.92f else {
				0.7f
			}
			angel += f2 * 0.1f
			angelOffset += f1 * 0.1f
			f2 *= 0.9f
			f1 *= 0.75f
			f2 += (localRandom.nextFloat() - localRandom.nextFloat()) * localRandom.nextFloat() * 2.0f
			f1 += (localRandom.nextFloat() - localRandom.nextFloat()) * localRandom.nextFloat() * 4.0f
			if (!isLargeCave && angle == randomAngel && radius > 1.0f && maxAngle > 0) {
				generateCaveNode(localRandom.nextLong(), chunk, x, y, z, localRandom.nextFloat() * 0.5f + 0.5f, angelOffset - 1.570796f, angel / 3.0f, angle, maxAngle, 1.0)
				generateCaveNode(localRandom.nextLong(), chunk, x, y, z, localRandom.nextFloat() * 0.5f + 0.5f, angelOffset + 1.570796f, angel / 3.0f, angle, maxAngle, 1.0)
				return
			}
			if (!isLargeCave && localRandom.nextInt(4) == 0) {
				angle++
				continue
			}

			// Check if distance to working point (x and z) too larger than working radius (maybe ??)
			val distanceX = x - realX
			val distanceZ = z - realZ
			val angelDiff = maxAngle - angle.toDouble()
			val newRadius = radius + 2.0f + 16.0f.toDouble()
			if (distanceX * distanceX + distanceZ * distanceZ - angelDiff * angelDiff > newRadius * newRadius) {
				return
			}

			//Boundaries check.
			if (x < realX - 16.0 - offsetXZ * 2.0 || z < realZ - 16.0 - offsetXZ * 2.0 || x > realX + 16.0 + offsetXZ * 2.0 || z > realZ + 16.0 + offsetXZ * 2.0) {
				angle++
				continue
			}
			var xFrom = floor(x - offsetXZ) - chunkX * 16 - 1
			var xTo = floor(x + offsetXZ) - chunkX * 16 + 1
			var yFrom = floor(y - offsetY) - 1
			var yTo = floor(y + offsetY) + 1
			var zFrom = floor(z - offsetXZ) - chunkZ * 16 - 1
			var zTo = floor(z + offsetXZ) - chunkZ * 16 + 1
			if (xFrom < 0) xFrom = 0
			if (xTo > 16) xTo = 16
			if (yFrom < 1) yFrom = 1
			if (yTo > worldHeightCap - 8) {
				yTo = worldHeightCap - 8
			}
			if (zFrom < 0) zFrom = 0
			if (zTo > 16) zTo = 16

			// Search for water
			var waterFound = false
			run {
				var xx = xFrom
				while (!waterFound && xx < xTo) {
					var zz = zFrom
					while (!waterFound && zz < zTo) {
						var yy = yTo + 1
						while (!waterFound && yy >= yFrom - 1) {
							if (yy >= 0 && yy < this.worldHeightCap) {
								val block = chunk.getBlockId(xx, yy, zz)
								if (block == Block.WATER || block == Block.STILL_WATER) {
									waterFound = true
								}
								if (yy != yFrom - 1 && xx != xFrom && xx != xTo - 1 && zz != zFrom && zz != zTo - 1) yy = yFrom
							}
							yy--
						}
						zz++
					}
					xx++
				}
			}
			if (waterFound) {
				angle++
				continue
			}

			// Generate cave
			for (xx in xFrom until xTo) {
				val modX = (xx + chunkX * 16 + 0.5 - x) / offsetXZ
				for (zz in zFrom until zTo) {
					val modZ = (zz + chunkZ * 16 + 0.5 - z) / offsetXZ
					var grassFound = false
					if (modX * modX + modZ * modZ < 1.0) {
						for (yy in yTo downTo yFrom + 1) {
							val modY = (yy - 1 + 0.5 - y) / offsetY
							if (modY > -0.7 && modX * modX + modY * modY + modZ * modZ < 1.0) {
								val biome = EnumBiome.getBiome(chunk.getBiomeId(xx, zz)) as? CoveredBiome ?: continue
								val material = chunk.getBlockId(xx, yy, zz)
								val materialAbove = chunk.getBlockId(xx, yy + 1, zz)
								if (material == Block.GRASS || material == Block.MYCELIUM) {
									grassFound = true
								}
								//TODO: check this
//								if (this.isSuitableBlock(material, materialAbove, biome))
								run {
									if (yy - 1 < 10) {
										chunk.setBlock(xx, yy, zz, Block.LAVA)
									} else {
										chunk.setBlock(xx, yy, zz, Block.AIR)

										// If grass was just deleted, try to
										// move it down
										if (grassFound && chunk.getBlockId(xx, yy - 1, zz) == Block.DIRT) {
											chunk.setBlock(xx, yy - 1, zz, biome.getSurfaceBlock(yy - 1))
										}
									}
								}
							}
						}
					}
				}
			}
			if (isLargeCave) {
				break
			}
			angle++
		}
	}

	protected fun generateChunk(chunkX: Int, chunkZ: Int, generatingChunkBuffer: FullChunk) {
		var i = random!!.nextInt(random!!.nextInt(random!!.nextInt(caveFrequency) + 1) + 1)
		if (evenCaveDistribution) i = caveFrequency
		if (random!!.nextInt(100) >= caveRarity) i = 0
		for (j in 0 until i) {
			val x = chunkX * 16 + random!!.nextInt(16).toDouble()
			var y: Double
			y = if (evenCaveDistribution) numberInRange(random, caveMinAltitude, caveMaxAltitude).toDouble() else random!!.nextInt(random!!.nextInt(caveMaxAltitude - caveMinAltitude + 1) + 1) + caveMinAltitude.toDouble()
			val z = chunkZ * 16 + random!!.nextInt(16).toDouble()
			var count = caveSystemFrequency
			var largeCaveSpawned = false
			if (random!!.nextInt(100) <= individualCaveRarity) {
				generateLargeCaveNode(random!!.nextLong(), generatingChunkBuffer, x, y, z)
				largeCaveSpawned = true
			}
			if (largeCaveSpawned || random!!.nextInt(100) <= caveSystemPocketChance - 1) {
				count += numberInRange(random, caveSystemPocketMinSize, caveSystemPocketMaxSize)
			}
			while (count > 0) {
				count--
				val f1 = random!!.nextFloat() * 3.141593f * 2.0f
				val f2 = (random!!.nextFloat() - 0.5f) * 2.0f / 8.0f
				val f3 = random!!.nextFloat() * 2.0f + random!!.nextFloat()
				generateCaveNode(random!!.nextLong(), generatingChunkBuffer, x, y, z, f3, f1, f2, 0, 0, 1.0)
			}
		}
	}

	companion object {
		var caveRarity = 7 //7
		var caveFrequency = 40 //40
		var caveMinAltitude = 8
		var caveMaxAltitude = 67
		var individualCaveRarity = 25 //25
		var caveSystemFrequency = 1
		var caveSystemPocketChance = 0
		var caveSystemPocketMinSize = 0
		var caveSystemPocketMaxSize = 4
		var evenCaveDistribution = false
		fun numberInRange(random: Random?, min: Int, max: Int): Int {
			return min + random!!.nextInt(max - min + 1)
		}
	}
}