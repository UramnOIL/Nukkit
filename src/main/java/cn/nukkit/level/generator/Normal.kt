package cn.nukkit.level.generator

import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.block.BlockStone
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.biome.Biome
import cn.nukkit.level.biome.BiomeSelector
import cn.nukkit.level.biome.EnumBiome
import cn.nukkit.level.generator
import cn.nukkit.level.generator.noise.vanilla.f.NoiseGeneratorOctavesF
import cn.nukkit.level.generator.noise.vanilla.f.NoiseGeneratorPerlinF
import cn.nukkit.level.generator.populator.impl.PopulatorBedrock
import cn.nukkit.level.generator.populator.impl.PopulatorCaves
import cn.nukkit.level.generator.populator.impl.PopulatorGroundCover
import cn.nukkit.level.generator.populator.impl.PopulatorOre
import cn.nukkit.level.generator.populator.type.Populator
import cn.nukkit.math.MathHelper.denormalizeClamp
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3
import java.util.*
import java.util.function.Supplier

/**
 * Nukkit's terrain generator
 * Originally adapted from the PocketMine-MP generator by NycuRO and CreeperFace
 * Mostly rewritten by DaPorkchop_
 *
 *
 * The following classes, and others related to terrain generation are theirs and are intended for NUKKIT USAGE and should not be copied/translated to other server software
 * such as BukkitPE, ClearSky, Genisys, PocketMine-MP, or others
 *
 *
 * Normal.java
 * MushroomPopulator.java
 * DarkOakTreePopulator.java
 * JungleBigTreePopulator.java
 * JungleTreePopulaotr.java
 * SavannaTreePopulator.java
 * SwampTreePopulator.java
 * BasicPopulator.java
 * TreeGenerator.java
 * HugeTreesGenerator.java
 * BeachBiome.java
 * ColdBeachBiome.java
 * DesertBiome.java
 * DesertHillsBiome.java
 * DesertMBiome.java
 * ExtremeHillsBiome.java
 * ExtremeHillsEdgeBiome.java
 * ExtremeHillsMBiome.java
 * ExtremeHillsPlusBiome.java
 * ExtremeHillsPlusMBiome.java
 * StoneBeachBiome.java
 * FlowerForestBiome.java
 * ForestBiome.java
 * ForestHillsBiome.java
 * IcePlainsBiome.java
 * IcePlainsSpikesBiome.java
 * JungleBiome.java
 * JungleEdgeBiome.java
 * JungleEdgeMBiome.java
 * JungleHillsBiome.java
 * JungleMBiome.java
 * MesaBiome.java
 * MesaBryceBiome.java
 * MesaPlateauBiome.java
 * MesaPlateauFBiome.java
 * MesaPlateauFMBiome.java
 * MesaPlateauMBiome.java
 * MushroomIslandBiome.java
 * MushroomIslandShoreBiome.java
 * DeepOceanBiome.java
 * FrozenOceanBiome.java
 * OceanBiome.java
 * PlainsBiome.java
 * SunflowerPlainsBiome.java
 * FrozenRiverBiome.java
 * RiverBiome.java
 * RoofedForestBiome.java
 * RoofedForestMBiome.java
 * SavannaBiome.java
 * SavannaMBiome.java
 * SavannaPlateauBiome.java
 * SavannaPlateauMBiome.java
 * SwampBiome.java
 * SwamplandMBiome.java
 * ColdTaigaBiome.java
 * ColdTaigaHillsBiome.java
 * ColdTaigaMBiome.java
 * MegaSpruceTaigaBiome.java
 * MegaTaigaBiome.java
 * MegaTagaHillsBiome.java
 * TaigaBiome.java
 * TaigaHillsBiome.java
 * TaigaMBiome.java
 * CoveredBiome.java
 * GrassyBiome.java
 * SandyBiome.java
 * WateryBiome.java
 * EnumBiomeBiome.java
 * PopulatorCount.java
 * PopulatorSurfaceBlock.java
 * Normal.java
 * Nether.java
 * End.java
 */
class Normal @JvmOverloads constructor(options: Map<String?, Any?>? = HashMap()) : Generator() {
	companion object {
		private val biomeWeights = FloatArray(25)
		const val seaHeight = 64

		init {
			for (i in -2..2) {
				for (j in -2..2) {
					biomeWeights[i + 2 + (j + 2) * 5] = (10.0f / Math.sqrt((i * i + j * j).toFloat() + 0.2f.toDouble())).toFloat()
				}
			}
		}
	}

	private val populators: MutableList<Populator> = ArrayList()
	private val generationPopulators: MutableList<Populator> = ArrayList()
	var scaleNoise: NoiseGeneratorOctavesF? = null
	var depthNoise: NoiseGeneratorOctavesF? = null
	override var chunkManager: ChunkManager? = null
		private set
	private var random: Random? = null
	private var nukkitRandom: NukkitRandom? = null
	private var localSeed1: Long = 0
	private var localSeed2: Long = 0
	private var selector: BiomeSelector? = null
	private val biomes: ThreadLocal<Array<Biome>> = ThreadLocal.withInitial(Supplier { arrayOfNulls(10 * 10) })
	private val depthRegion = ThreadLocal.withInitial<FloatArray?> { null }
	private val mainNoiseRegion = ThreadLocal.withInitial<FloatArray?> { null }
	private val minLimitRegion = ThreadLocal.withInitial<FloatArray?> { null }
	private val maxLimitRegion = ThreadLocal.withInitial<FloatArray?> { null }
	private val heightMap = ThreadLocal.withInitial { FloatArray(825) }
	private var minLimitPerlinNoise: NoiseGeneratorOctavesF? = null
	private var maxLimitPerlinNoise: NoiseGeneratorOctavesF? = null
	private var mainPerlinNoise: NoiseGeneratorOctavesF? = null
	private var surfaceNoise: NoiseGeneratorPerlinF? = null
	override val id: Int
		get() = Generator.Companion.TYPE_INFINITE

	override val name: String
		get() = "normal"

	override val settings: Map<String, Any>
		get() = HashMap()

	fun pickBiome(x: Int, z: Int): Biome {
		return selector!!.pickBiome(x, z)
	}

	override fun init(level: ChunkManager?, random: NukkitRandom?) {
		chunkManager = level
		nukkitRandom = random
		this.random = Random()
		nukkitRandom!!.setSeed(chunkManager!!.seed)
		localSeed1 = this.random!!.nextLong()
		localSeed2 = this.random!!.nextLong()
		nukkitRandom!!.setSeed(chunkManager!!.seed)
		selector = BiomeSelector(nukkitRandom)
		minLimitPerlinNoise = NoiseGeneratorOctavesF(random, 16)
		maxLimitPerlinNoise = NoiseGeneratorOctavesF(random, 16)
		mainPerlinNoise = NoiseGeneratorOctavesF(random, 8)
		surfaceNoise = NoiseGeneratorPerlinF(random, 4)
		scaleNoise = NoiseGeneratorOctavesF(random, 10)
		depthNoise = NoiseGeneratorOctavesF(random, 16)

		//this should run before all other populators so that we don't do things like generate ground cover on bedrock or something
		val cover = PopulatorGroundCover()
		generationPopulators.add(cover)
		val bedrock = PopulatorBedrock()
		generationPopulators.add(bedrock)
		val ores = PopulatorOre()
		ores.setOreTypes(arrayOf(
				OreType(get(BlockID.COAL_ORE), 20, 17, 0, 128),
				OreType(get(BlockID.IRON_ORE), 20, 9, 0, 64),
				OreType(get(BlockID.REDSTONE_ORE), 8, 8, 0, 16),
				OreType(get(BlockID.LAPIS_ORE), 1, 7, 0, 16),
				OreType(get(BlockID.GOLD_ORE), 2, 9, 0, 32),
				OreType(get(BlockID.DIAMOND_ORE), 1, 8, 0, 16),
				OreType(get(BlockID.DIRT), 10, 33, 0, 128),
				OreType(get(BlockID.GRAVEL), 8, 33, 0, 128),
				OreType(get(BlockID.STONE, BlockStone.GRANITE), 10, 33, 0, 80),
				OreType(get(BlockID.STONE, BlockStone.DIORITE), 10, 33, 0, 80),
				OreType(get(BlockID.STONE, BlockStone.ANDESITE), 10, 33, 0, 80)
		))
		populators.add(ores)
		val caves = PopulatorCaves()
		populators.add(caves)

		//TODO: fix ravines
		//PopulatorRavines ravines = new PopulatorRavines();
		//this.populators.add(ravines);
	}

	override fun generateChunk(chunkX: Int, chunkZ: Int) {
		val baseX = chunkX shl 4
		val baseZ = chunkZ shl 4
		nukkitRandom!!.setSeed(chunkX * localSeed1 xor chunkZ * localSeed2 xor chunkManager!!.seed)
		val chunk = chunkManager!!.getChunk(chunkX, chunkZ)

		//generate base noise values
		val depthRegion = depthNoise!!.generateNoiseOctaves(depthRegion.get(), chunkX * 4, chunkZ * 4, 5, 5, 200f, 200f, 0.5f)
		this.depthRegion.set(depthRegion)
		val mainNoiseRegion = mainPerlinNoise!!.generateNoiseOctaves(mainNoiseRegion.get(), chunkX * 4, 0, chunkZ * 4, 5, 33, 5, 684.412f / 60f, 684.412f / 160f, 684.412f / 60f)
		this.mainNoiseRegion.set(mainNoiseRegion)
		val minLimitRegion = minLimitPerlinNoise!!.generateNoiseOctaves(minLimitRegion.get(), chunkX * 4, 0, chunkZ * 4, 5, 33, 5, 684.412f, 684.412f, 684.412f)
		this.minLimitRegion.set(minLimitRegion)
		val maxLimitRegion = maxLimitPerlinNoise!!.generateNoiseOctaves(maxLimitRegion.get(), chunkX * 4, 0, chunkZ * 4, 5, 33, 5, 684.412f, 684.412f, 684.412f)
		this.maxLimitRegion.set(maxLimitRegion)
		val heightMap = heightMap.get()

		//generate heightmap and smooth biome heights
		var horizCounter = 0
		var vertCounter = 0
		for (xSeg in 0..4) {
			for (zSeg in 0..4) {
				var heightVariationSum = 0.0f
				var baseHeightSum = 0.0f
				var biomeWeightSum = 0.0f
				val biome = pickBiome(baseX + xSeg * 4, baseZ + zSeg * 4)
				for (xSmooth in -2..2) {
					for (zSmooth in -2..2) {
						val biome1 = pickBiome(baseX + xSeg * 4 + xSmooth, baseZ + zSeg * 4 + zSmooth)
						val baseHeight = biome1.baseHeight
						val heightVariation = biome1.heightVariation
						var scaledWeight = biomeWeights[xSmooth + 2 + (zSmooth + 2) * 5] / (baseHeight + 2.0f)
						if (biome1.baseHeight > biome.baseHeight) {
							scaledWeight /= 2.0f
						}
						heightVariationSum += heightVariation * scaledWeight
						baseHeightSum += baseHeight * scaledWeight
						biomeWeightSum += scaledWeight
					}
				}
				heightVariationSum = heightVariationSum / biomeWeightSum
				baseHeightSum = baseHeightSum / biomeWeightSum
				heightVariationSum = heightVariationSum * 0.9f + 0.1f
				baseHeightSum = (baseHeightSum * 4.0f - 1.0f) / 8.0f
				var depthNoise = depthRegion[vertCounter] / 8000.0f
				if (depthNoise < 0.0f) {
					depthNoise = -depthNoise * 0.3f
				}
				depthNoise = depthNoise * 3.0f - 2.0f
				if (depthNoise < 0.0f) {
					depthNoise = depthNoise / 2.0f
					if (depthNoise < -1.0f) {
						depthNoise = -1.0f
					}
					depthNoise = depthNoise / 1.4f
					depthNoise = depthNoise / 2.0f
				} else {
					if (depthNoise > 1.0f) {
						depthNoise = 1.0f
					}
					depthNoise = depthNoise / 8.0f
				}
				++vertCounter
				var baseHeightClone = baseHeightSum
				val heightVariationClone = heightVariationSum
				baseHeightClone = baseHeightClone + depthNoise * 0.2f
				baseHeightClone = baseHeightClone * 8.5f / 8.0f
				val baseHeightFactor = 8.5f + baseHeightClone * 4.0f
				for (ySeg in 0..32) {
					var baseScale = (ySeg.toFloat() - baseHeightFactor) * 12f * 128.0f / 256.0f / heightVariationClone
					if (baseScale < 0.0f) {
						baseScale *= 4.0f
					}
					val minScaled = minLimitRegion[horizCounter] / 512f
					val maxScaled = maxLimitRegion[horizCounter] / 512f
					val noiseScaled = (mainNoiseRegion[horizCounter] / 10.0f + 1.0f) / 2.0f
					var clamp = denormalizeClamp(minScaled, maxScaled, noiseScaled) - baseScale
					if (ySeg > 29) {
						val yScaled = (ySeg - 29) as Float / 3.0f
						clamp = clamp * (1.0f - yScaled) + -10.0f * yScaled
					}
					heightMap[horizCounter] = clamp
					++horizCounter
				}
			}
		}

		//place blocks
		for (xSeg in 0..3) {
			val xScale = xSeg * 5
			val xScaleEnd = (xSeg + 1) * 5
			for (zSeg in 0..3) {
				val zScale1 = (xScale + zSeg) * 33
				val zScaleEnd1 = (xScale + zSeg + 1) * 33
				val zScale2 = (xScaleEnd + zSeg) * 33
				val zScaleEnd2 = (xScaleEnd + zSeg + 1) * 33
				for (ySeg in 0..31) {
					var height1 = heightMap[zScale1 + ySeg].toDouble()
					var height2 = heightMap[zScaleEnd1 + ySeg].toDouble()
					var height3 = heightMap[zScale2 + ySeg].toDouble()
					var height4 = heightMap[zScaleEnd2 + ySeg].toDouble()
					val height5 = (heightMap[zScale1 + ySeg + 1] - height1) * 0.125f
					val height6 = (heightMap[zScaleEnd1 + ySeg + 1] - height2) * 0.125f
					val height7 = (heightMap[zScale2 + ySeg + 1] - height3) * 0.125f
					val height8 = (heightMap[zScaleEnd2 + ySeg + 1] - height4) * 0.125f
					for (yIn in 0..7) {
						var baseIncr = height1
						var baseIncr2 = height2
						val scaleY = (height3 - height1) * 0.25f
						val scaleY2 = (height4 - height2) * 0.25f
						for (zIn in 0..3) {
							val scaleZ = (baseIncr2 - baseIncr) * 0.25f
							var scaleZ2 = baseIncr - scaleZ
							for (xIn in 0..3) {
								if (scaleZ.let { scaleZ2 += it; scaleZ2 } > 0.0f) {
									chunk!!.setBlockId(xSeg * 4 + zIn, ySeg * 8 + yIn, zSeg * 4 + xIn, BlockID.STONE)
								} else if (ySeg * 8 + yIn <= seaHeight) {
									chunk!!.setBlockId(xSeg * 4 + zIn, ySeg * 8 + yIn, zSeg * 4 + xIn, BlockID.STILL_WATER)
								}
							}
							baseIncr += scaleY
							baseIncr2 += scaleY2
						}
						height1 += height5
						height2 += height6
						height3 += height7
						height4 += height8
					}
				}
			}
		}
		for (x in 0..15) {
			for (z in 0..15) {
				val biome = selector!!.pickBiome(baseX or x, baseZ or z)
				chunk!!.setBiome(x, z, biome)
			}
		}

		//populate chunk
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
		get() = Vector3(0.5, 256, 0.5)
}