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
 *
 * Used for the dense foliage on the floor in the jungle M biome
 */
class JungleFloorPopulator @JvmOverloads constructor(private val type: Int = BlockSapling.JUNGLE) : Populator() {
	private var level: ChunkManager? = null
	private var randomAmount = 0
	private var baseAmount = 0
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
			NewJungleTree(1, 0).generate(level, random, v.setComponents(x.toDouble(), y.toDouble(), z.toDouble()))
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