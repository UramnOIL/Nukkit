package cn.nukkit.level.generator.`object`.ore

import cn.nukkit.block.Block
import cn.nukkit.block.BlockID
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.generator
import cn.nukkit.math.MathHelper.cos
import cn.nukkit.math.MathHelper.floor
import cn.nukkit.math.MathHelper.sin
import cn.nukkit.math.NukkitRandom

/**
 * author: MagicDroidX
 * Nukkit Project
 */
//porktodo: rewrite this, the whole class is terrible and generated ores look stupid
class OreType @JvmOverloads constructor(material: Block, clusterCount: Int, clusterSize: Int, minHeight: Int, maxHeight: Int, replaceBlockId: Int = BlockID.STONE) {
	val fullId: Int
	val clusterCount: Int
	val clusterSize: Int
	val maxHeight: Int
	val minHeight: Int
	val replaceBlockId: Int
	fun spawn(level: ChunkManager, rand: NukkitRandom, replaceId: Int, x: Int, y: Int, z: Int): Boolean {
		val piScaled = rand.nextFloat() * Math.PI.toFloat()
		val scaleMaxX = ((x + 8).toFloat() + sin(piScaled) * clusterSize.toFloat() / 8.0f).toDouble()
		val scaleMinX = ((x + 8).toFloat() - sin(piScaled) * clusterSize.toFloat() / 8.0f).toDouble()
		val scaleMaxZ = ((z + 8).toFloat() + cos(piScaled) * clusterSize.toFloat() / 8.0f).toDouble()
		val scaleMinZ = ((z + 8).toFloat() - cos(piScaled) * clusterSize.toFloat() / 8.0f).toDouble()
		val scaleMaxY = (y + rand.nextBoundedInt(3) - 2).toDouble()
		val scaleMinY = (y + rand.nextBoundedInt(3) - 2).toDouble()
		for (i in 0 until clusterSize) {
			val sizeIncr = i.toFloat() / clusterSize.toFloat()
			val scaleX = scaleMaxX + (scaleMinX - scaleMaxX) * sizeIncr.toDouble()
			val scaleY = scaleMaxY + (scaleMinY - scaleMaxY) * sizeIncr.toDouble()
			val scaleZ = scaleMaxZ + (scaleMinZ - scaleMaxZ) * sizeIncr.toDouble()
			val randSizeOffset = rand.nextDouble() * clusterSize.toDouble() / 16.0
			val randVec1 = (sin(Math.PI.toFloat() * sizeIncr) + 1.0f).toDouble() * randSizeOffset + 1.0
			val randVec2 = (sin(Math.PI.toFloat() * sizeIncr) + 1.0f).toDouble() * randSizeOffset + 1.0
			val minX = floor(scaleX - randVec1 / 2.0)
			val minY = floor(scaleY - randVec2 / 2.0)
			val minZ = floor(scaleZ - randVec1 / 2.0)
			val maxX = floor(scaleX + randVec1 / 2.0)
			val maxY = floor(scaleY + randVec2 / 2.0)
			val maxZ = floor(scaleZ + randVec1 / 2.0)
			for (xSeg in minX..maxX) {
				val xVal = (xSeg.toDouble() + 0.5 - scaleX) / (randVec1 / 2.0)
				if (xVal * xVal < 1.0) {
					for (ySeg in minY..maxY) {
						val yVal = (ySeg.toDouble() + 0.5 - scaleY) / (randVec2 / 2.0)
						if (xVal * xVal + yVal * yVal < 1.0) {
							for (zSeg in minZ..maxZ) {
								val zVal = (zSeg.toDouble() + 0.5 - scaleZ) / (randVec1 / 2.0)
								if (xVal * xVal + yVal * yVal + zVal * zVal < 1.0) {
									if (level.getBlockIdAt(xSeg, ySeg, zSeg) == replaceBlockId) {
										level.setBlockFullIdAt(xSeg, ySeg, zSeg, fullId)
									}
								}
							}
						}
					}
				}
			}
		}
		return true
	}

	init {
		fullId = material.fullId
		this.clusterCount = clusterCount
		this.clusterSize = clusterSize
		this.maxHeight = maxHeight
		this.minHeight = minHeight
		this.replaceBlockId = replaceBlockId
	}
}