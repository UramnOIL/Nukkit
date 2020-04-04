package cn.nukkit.level.generator.populator.impl.tree

import cn.nukkit.block.Block
import cn.nukkit.block.BlockSapling
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator
import cn.nukkit.level.generator.populator.type.Populator
import cn.nukkit.math.NukkitMath.randomRange
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3

/**
 * @author DaPorkchop_
 */
class SpruceMegaTreePopulator private constructor(private val type: Int) : Populator() {
	private var level: ChunkManager? = null
	private var randomAmount = 0
	private var baseAmount = 0

	constructor() : this(BlockSapling.SPRUCE) {}

	fun setRandomAmount(randomAmount: Int) {
		this.randomAmount = randomAmount
	}

	fun setBaseAmount(baseAmount: Int) {
		this.baseAmount = baseAmount
	}

	override fun populate(level: ChunkManager, chunkX: Int, chunkZ: Int, random: NukkitRandom, chunk: FullChunk) {
		this.level = level
		val amount = random.nextBoundedInt(randomAmount + 1) + baseAmount
		val v = Vector3()
		for (i in 0 until amount) {
			val x = randomRange(random, chunkX shl 4, (chunkX shl 4) + 15)
			val z = randomRange(random, chunkZ shl 4, (chunkZ shl 4) + 15)
			val y = this.getHighestWorkableBlock(x, z)
			if (y == -1) {
				continue
			}
			ObjectBigSpruceTree(1 / 4f, 5).placeObject(this.level, x.also { v.x = it }, y.also { v.y = it }, z.also { v.z = it }, random)
		}
	}

	private fun getHighestWorkableBlock(x: Int, z: Int): Int {
		var y: Int
		y = 255
		while (y > 0) {
			val b = level!!.getBlockIdAt(x, y, z)
			if (b == Block.DIRT || b == Block.GRASS) {
				break
			} else if (b != Block.AIR && b != Block.SNOW_LAYER) {
				return -1
			}
			--y
		}
		return ++y
	}

}