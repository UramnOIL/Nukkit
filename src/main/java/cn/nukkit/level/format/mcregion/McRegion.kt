package cn.nukkit.level.format.mcregion

import cn.nukkit.blockentity.BlockEntitySpawnable
import cn.nukkit.level.Level
import cn.nukkit.level.format.ChunkSection
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.format.LevelProvider
import cn.nukkit.level.format.generic.BaseFullChunk
import cn.nukkit.level.format.generic.BaseLevelProvider
import cn.nukkit.level.format.generic.BaseRegionLoader
import cn.nukkit.level.generator.Generator
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.scheduler.AsyncTask
import cn.nukkit.utils.BinaryStream
import cn.nukkit.utils.ChunkException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteOrder
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class McRegion(level: Level?, path: String) : BaseLevelProvider(level, path) {
	@Throws(ChunkException::class)
	override fun requestChunkTask(x: Int, z: Int): AsyncTask? {
		val chunk = this.getChunk(x, z, false) ?: throw ChunkException("Invalid Chunk Sent")
		val timestamp = chunk.changes
		var tiles = ByteArray(0)
		if (!chunk.getBlockEntities().isEmpty()) {
			val tagList: MutableList<CompoundTag?> = ArrayList()
			for (blockEntity in chunk.getBlockEntities().values) {
				if (blockEntity is BlockEntitySpawnable) {
					tagList.add((blockEntity as BlockEntitySpawnable).spawnCompound)
				}
			}
			tiles = try {
				NBTIO.write(tagList, ByteOrder.LITTLE_ENDIAN, true)
			} catch (e: IOException) {
				throw RuntimeException(e)
			}
		}
		val extra: Map<Int, Int> = chunk.getBlockExtraDataArray()
		val extraData: BinaryStream?
		if (!extra.isEmpty()) {
			extraData = BinaryStream()
			extraData.putLInt(extra.size)
			for ((key, value) in extra) {
				extraData.putLInt(key)
				extraData.putLShort(value)
			}
		} else {
			extraData = null
		}
		val stream = BinaryStream()
		stream.put(chunk.getBlockIdArray())
		stream.put(chunk.getBlockDataArray())
		stream.put(chunk.getBlockSkyLightArray())
		stream.put(chunk.getBlockLightArray())
		stream.put(chunk.getHeightMapArray())
		stream.put(chunk.getBiomeIdArray())
		if (extraData != null) {
			stream.put(extraData.buffer)
		} else {
			stream.putLInt(0)
		}
		stream.put(tiles)
		this.getLevel().chunkRequestCallback(timestamp, x, z, 16, stream.buffer)
		return null
	}

	override fun loadChunk(index: Long, chunkX: Int, chunkZ: Int, create: Boolean): BaseFullChunk? {
		val regionX: Int = BaseLevelProvider.Companion.getRegionIndexX(chunkX)
		val regionZ: Int = BaseLevelProvider.Companion.getRegionIndexZ(chunkZ)
		val region = loadRegion(regionX, regionZ)
		this.level!!.timings.syncChunkLoadDataTimer.startTiming()
		var chunk: BaseFullChunk?
		chunk = try {
			region.readChunk(chunkX - regionX * 32, chunkZ - regionZ * 32)
		} catch (e: IOException) {
			throw RuntimeException(e)
		}
		if (chunk == null) {
			if (create) {
				chunk = getEmptyChunk(chunkX, chunkZ)
				putChunk(index, chunk!!)
			}
		} else {
			putChunk(index, chunk)
		}
		this.level!!.timings.syncChunkLoadDataTimer.stopTiming()
		return chunk
	}

	override fun getEmptyChunk(chunkX: Int, chunkZ: Int): Chunk? {
		return Chunk.Companion.getEmptyChunk(chunkX, chunkZ, this)
	}

	override fun saveChunk(x: Int, z: Int) {
		if (this.isChunkLoaded(x, z)) {
			try {
				getRegion(x shr 5, z shr 5).writeChunk(this.getChunk(x, z))
			} catch (e: Exception) {
				throw RuntimeException(e)
			}
		}
	}

	override fun saveChunk(x: Int, z: Int, chunk: FullChunk?) {
		if (chunk !is Chunk) {
			throw ChunkException("Invalid Chunk class")
		}
		loadRegion(x shr 5, z shr 5)
		chunk.setPosition(x, z)
		try {
			getRegion(x shr 5, z shr 5).writeChunk(chunk)
		} catch (e: Exception) {
			throw RuntimeException(e)
		}
	}

	protected fun loadRegion(x: Int, z: Int): BaseRegionLoader {
		val tmp = lastRegion.get()
		if (tmp != null && x == tmp.x && z == tmp.z) {
			return tmp
		}
		val index = Level.chunkHash(x, z)
		synchronized(regions) {
			var region = regions[index]
			if (region == null) {
				region = RegionLoader(this, x, z)
				regions[index] = region
			}
			lastRegion.set(region)
			return region
		}
	}

	companion object {
		val providerName: String
			get() = "mcregion"

		val providerOrder: Byte
			get() = LevelProvider.ORDER_ZXY

		fun usesChunkSection(): Boolean {
			return false
		}

		fun isValid(path: String): Boolean {
			var isValid = File("$path/level.dat").exists() && File("$path/region/").isDirectory
			if (isValid) {
				for (file in File("$path/region/").listFiles { dir: File?, name: String? -> Pattern.matches("^.+\\.mc[r|a]$", name) }) {
					if (!file.name.endsWith(".mcr")) {
						isValid = false
						break
					}
				}
			}
			return isValid
		}

		@JvmOverloads
		@Throws(IOException::class)
		fun generate(path: String, name: String?, seed: Long, generator: Class<out Generator?>?, options: Map<String?, String?> = HashMap()) {
			if (!File("$path/region").exists()) {
				File("$path/region").mkdirs()
			}
			val levelData = CompoundTag("Data")
					.putCompound("GameRules", CompoundTag())
					.putLong("DayTime", 0)
					.putInt("GameType", 0)
					.putString("generatorName", Generator.getGeneratorName(generator))
					.putString("generatorOptions", options.getOrDefault("preset", ""))
					.putInt("generatorVersion", 1)
					.putBoolean("hardcore", false)
					.putBoolean("initialized", true)
					.putLong("LastPlayed", System.currentTimeMillis() / 1000)
					.putString("LevelName", name)
					.putBoolean("raining", false)
					.putInt("rainTime", 0)
					.putLong("RandomSeed", seed)
					.putInt("SpawnX", 128)
					.putInt("SpawnY", 70)
					.putInt("SpawnZ", 128)
					.putBoolean("thundering", false)
					.putInt("thunderTime", 0)
					.putInt("version", 19133)
					.putLong("Time", 0)
					.putLong("SizeOnDisk", 0)
			NBTIO.writeGZIPCompressed(CompoundTag().putCompound("Data", levelData), FileOutputStream(path + "level.dat"), ByteOrder.BIG_ENDIAN)
		}

		fun createChunkSection(y: Int): ChunkSection? {
			return null
		}
	}
}