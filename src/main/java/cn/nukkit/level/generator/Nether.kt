package cn.nukkit.level.generator

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.Level
import cn.nukkit.level.biome.EnumBiome
import cn.nukkit.level.generator
import cn.nukkit.level.generator.noise.nukkit.f.SimplexF
import cn.nukkit.level.generator.populator.impl.PopulatorGlowStone
import cn.nukkit.level.generator.populator.impl.PopulatorGroundFire
import cn.nukkit.level.generator.populator.impl.PopulatorLava
import cn.nukkit.level.generator.populator.impl.PopulatorOre
import cn.nukkit.level.generator.populator.type.Populator
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3
import java.util.*

class Nether @JvmOverloads constructor(options: Map<String?, Any?>? = HashMap()) : Generator() {
	override var chunkManager: ChunkManager? = null
		private set

	/**
	 * @var Random
	 */
	private var nukkitRandom: NukkitRandom? = null
	private var random: Random? = null
	private val lavaHeight = 32.0
	private val bedrockDepth = 5.0
	private val noiseGen = arrayOfNulls<SimplexF>(3)
	private val populators: MutableList<Populator> = ArrayList()
	private val generationPopulators: List<Populator> = ArrayList()
	private var localSeed1: Long = 0
	private var localSeed2: Long = 0
	override val id: Int
		get() = Generator.Companion.TYPE_NETHER

	override val dimension: Int
		get() = Level.DIMENSION_NETHER

	override val name: String
		get() = "nether"

	override val settings: Map<String, Any>
		get() = HashMap()

	override fun init(level: ChunkManager?, random: NukkitRandom?) {
		chunkManager = level
		nukkitRandom = random
		this.random = Random()
		nukkitRandom!!.setSeed(chunkManager!!.seed)
		for (i in noiseGen.indices) {
			noiseGen[i] = SimplexF(nukkitRandom, 4, 1 / 4f, 1 / 64f)
		}
		nukkitRandom!!.setSeed(chunkManager!!.seed)
		localSeed1 = this.random!!.nextLong()
		localSeed2 = this.random!!.nextLong()
		val ores = PopulatorOre(Block.NETHERRACK)
		ores.setOreTypes(arrayOf(
				OreType(get(BlockID.QUARTZ_ORE), 20, 16, 0, 128),
				OreType(get(BlockID.SOUL_SAND), 5, 64, 0, 128),
				OreType(get(BlockID.GRAVEL), 5, 64, 0, 128),
				OreType(get(BlockID.LAVA), 1, 16, 0, lavaHeight.toInt())))
		populators.add(ores)
		val groundFire = PopulatorGroundFire()
		groundFire.setBaseAmount(1)
		groundFire.setRandomAmount(1)
		populators.add(groundFire)
		val lava = PopulatorLava()
		lava.setBaseAmount(1)
		lava.setRandomAmount(2)
		populators.add(lava)
		populators.add(PopulatorGlowStone())
		val ore = PopulatorOre(Block.NETHERRACK)
		ore.setOreTypes(arrayOf(
				OreType(get(BlockID.QUARTZ_ORE), 40, 16, 0, 128, BlockID.NETHERRACK),
				OreType(get(BlockID.SOUL_SAND), 1, 64, 30, 35, BlockID.NETHERRACK),
				OreType(get(BlockID.LAVA), 32, 1, 0, 32, BlockID.NETHERRACK),
				OreType(get(BlockID.MAGMA), 32, 16, 26, 37, BlockID.NETHERRACK)))
		populators.add(ore)
	}

	override fun generateChunk(chunkX: Int, chunkZ: Int) {
		val baseX = chunkX shl 4
		val baseZ = chunkZ shl 4
		nukkitRandom!!.setSeed(chunkX * localSeed1 xor chunkZ * localSeed2 xor chunkManager!!.seed)
		val chunk = chunkManager!!.getChunk(chunkX, chunkZ)
		for (x in 0..15) {
			for (z in 0..15) {
				val biome = EnumBiome.HELL.biome
				chunk!!.setBiomeId(x, z, biome.id)
				chunk.setBlockId(x, 0, z, Block.BEDROCK)
				for (y in 115..126) {
					chunk.setBlockId(x, y, z, Block.NETHERRACK)
				}
				chunk.setBlockId(x, 127, z, Block.BEDROCK)
				for (y in 1..126) {
					if (getNoise(baseX or x, y, baseZ or z) > 0) {
						chunk.setBlockId(x, y, z, Block.NETHERRACK)
					} else if (y <= lavaHeight) {
						chunk.setBlockId(x, y, z, Block.STILL_LAVA)
						chunk.setBlockLight(x, y + 1, z, 15)
					}
				}
			}
		}
		for (populator in generationPopulators) {
			populator.populate(chunkManager, chunkX, chunkZ, nukkitRandom, chunk)
		}
	}

	override fun populateChunk(chunkX: Int, chunkZ: Int) {
		val chunk = chunkManager!!.getChunk(chunkX, chunkZ)
		nukkitRandom!!.setSeed((-0x21524111 xor (chunkX shl 8) xor chunkZ xor chunkManager!!.seed.toInt()).toLong())
		for (populator in populators) {
			populator.populate(chunkManager, chunkX, chunkZ, nukkitRandom, chunk)
		}
		val biome = EnumBiome.getBiome(chunk!!.getBiomeId(7, 7))
		biome.populateChunk(chunkManager, chunkX, chunkZ, nukkitRandom)
	}

	override val spawn: Vector3
		get() = Vector3(0, 64, 0)

	fun getNoise(x: Int, y: Int, z: Int): Float {
		var `val` = 0f
		for (i in noiseGen.indices) {
			`val` += noiseGen[i]!!.noise3D((x shr i.toFloat().toInt()).toFloat(), y.toFloat(), (z shr i.toFloat().toInt()).toFloat(), true)
		}
		return `val`
	}
}