package cn.nukkit.level.generator

import cn.nukkit.Server
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.block.BlockID
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator.`object`.ore.OreType
import cn.nukkit.level.generator.populator.impl.PopulatorOre
import cn.nukkit.level.generator.populator.type.Populator
import cn.nukkit.math.NukkitRandom
import cn.nukkit.math.Vector3
import java.util.*
import java.util.regex.Pattern

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class Flat constructor(private val options: Map<String, Any> = mapOf()) : Generator() {
	override val id = TYPE_FLAT
	private lateinit var level: ChunkManager
	private lateinit var random: NukkitRandom
	private val populators: MutableList<Populator> = listOf()
	private lateinit var structure: Array<IntArray?>
	private var floorLevel = 0
	private var preset: String? = "2;7,2x3,2;1;"
	private var init = false
	private var biome = 0
	override val chunkManager
		get() = level
	override val settings = options
	override val name: String = "flat"

	override fun init(level: ChunkManager, random: NukkitRandom) {
		this.level = level
		this.random = random
	}

	protected fun parsePreset(preset: String?, chunkX: Int, chunkZ: Int) {
		try {
			this.preset = preset
			val presetArray = preset!!.split(";").toTypedArray()
			val version = Integer.valueOf(presetArray[0])
			val blocks = if (presetArray.size > 1) presetArray[1] else ""
			biome = if (presetArray.size > 2) Integer.valueOf(presetArray[2]) else 1
			val options = if (presetArray.size > 3) presetArray[1] else ""
			structure = arrayOfNulls(256)
			var y = 0
			for (block in blocks.split(",").toTypedArray()) {
				var id: Int
				var meta = 0
				var cnt = 1
				if (Pattern.matches("^[0-9]{1,3}x[0-9]$", block)) {
					//AxB
					val s = block.split("x").toTypedArray()
					cnt = Integer.valueOf(s[0])
					id = Integer.valueOf(s[1])
				} else if (Pattern.matches("^[0-9]{1,3}:[0-9]{0,2}$", block)) {
					//A:B
					val s = block.split(":").toTypedArray()
					id = Integer.valueOf(s[0])
					meta = Integer.valueOf(s[1])
				} else if (Pattern.matches("^[0-9]{1,3}$", block)) {
					//A
					id = Integer.valueOf(block)
				} else {
					continue
				}
				var cY = y
				y += cnt
				if (y > 0xFF) {
					y = 0xFF
				}
				while (cY < y) {
					structure[cY] = intArrayOf(id, meta)
					++cY
				}
			}
			floorLevel = y
			while (y <= 0xFF) {
				structure[y] = intArrayOf(0, 0)
				++y
			}
			options.split(",").toTypedArray().forEach {
				if (Pattern.matches("^[0-9a-z_]+$", it)) {
					this.options[it] = true
				} else if (Pattern.matches("^[0-9a-z_]+\\([0-9a-z_ =]+\\)$", it)) {
					val name = it.substring(0, option.indexOf("("))
					val extra = option.substring(option.indexOf("(") + 1, option.indexOf(")"))
					val map: MutableMap<String, Float> = HashMap()
					for (kv in extra.split(" ").toTypedArray()) {
						val data = kv.split("=").toTypedArray()
						map[data[0]] = java.lang.Float.valueOf(data[1])
					}
					this.options[name] = map
				}
			}
		} catch (e: Exception) {
			Server.instance!!.logger.error("error while parsing the preset", e)
			throw RuntimeException(e)
		}
	}

	override fun generateChunk(chunkX: Int, chunkZ: Int) {
		if (!init) {
			init = true
			if (options.containsKey("preset") && "" != options["preset"]) {
				parsePreset(options["preset"] as String?, chunkX, chunkZ)
			} else {
				parsePreset(preset, chunkX, chunkZ)
			}
		}
		this.generateChunk(level!!.getChunk(chunkX, chunkZ))
	}

	private fun generateChunk(chunk: FullChunk?) {
		chunk!!.setGenerated()
		for (Z in 0..15) {
			for (X in 0..15) {
				chunk.setBiomeId(X, Z, biome)
				for (y in 0..255) {
					val k = structure[y]!![0]
					val l = structure[y]!![1]
					chunk.setBlock(X, y, Z, structure[y]!![0], structure[y]!![1])
				}
			}
		}
	}

	override fun populateChunk(chunkX: Int, chunkZ: Int) {
		val chunk = level!!.getChunk(chunkX, chunkZ)
		random!!.setSeed((-0x21524111 xor (chunkX shl 8) xor chunkZ xor level!!.seed.toInt()).toLong())
		for (populator in populators) {
			populator.populate(level, chunkX, chunkZ, random, chunk)
		}
	}

	override val spawn: Vector3 = Vector3(128, floorLevel.toDouble(), 128)

	init {
		if (options.containsKey("decoration")) {
			val ores = PopulatorOre()
			ores.setOreTypes(arrayOf(
					OreType(get(BlockID.COAL_ORE), 20, 16, 0, 128),
					OreType(get(BlockID.IRON_ORE), 20, 8, 0, 64),
					OreType(get(BlockID.REDSTONE_ORE), 8, 7, 0, 16),
					OreType(get(BlockID.LAPIS_ORE), 1, 6, 0, 32),
					OreType(get(BlockID.GOLD_ORE), 2, 8, 0, 32),
					OreType(get(BlockID.DIAMOND_ORE), 1, 7, 0, 16),
					OreType(get(BlockID.DIRT), 20, 32, 0, 128),
					OreType(get(BlockID.GRAVEL), 20, 16, 0, 128)))
			populators.add(ores)
		}
	}
}