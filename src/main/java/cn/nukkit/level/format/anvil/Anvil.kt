package cn.nukkit.level.format.anvil

import cn.nukkit.blockentity.BlockEntitySpawnable
import cn.nukkit.level.Level
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
import cn.nukkit.utils.ThreadCache
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
import kotlin.collections.indices
import kotlin.collections.iterator
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class Anvil(level: Level?, path: String) : BaseLevelProvider(level, path) {
	override fun getEmptyChunk(chunkX: Int, chunkZ: Int): Chunk? {
		return Chunk.Companion.getEmptyChunk(chunkX, chunkZ, this)
	}

	@Throws(ChunkException::class)
	override fun requestChunkTask(x: Int, z: Int): AsyncTask? {
		val chunk = this.getChunk(x, z, false) as Chunk? ?: throw ChunkException("Invalid Chunk Set")
		val timestamp = chunk.changes
		var blockEntities = ByteArray(0)
		if (!chunk.getBlockEntities().isEmpty()) {
			val tagList: MutableList<CompoundTag?> = ArrayList()
			for (blockEntity in chunk.getBlockEntities().values) {
				if (blockEntity is BlockEntitySpawnable) {
					tagList.add((blockEntity as BlockEntitySpawnable).spawnCompound)
				}
			}
			blockEntities = try {
				NBTIO.write(tagList, ByteOrder.LITTLE_ENDIAN, true)
			} catch (e: IOException) {
				throw RuntimeException(e)
			}
		}
		val extra: Map<Int, Int> = chunk.getBlockExtraDataArray()
		val extraData: BinaryStream?
		if (!extra.isEmpty()) {
			extraData = BinaryStream()
			extraData.putVarInt(extra.size)
			for ((key, value) in extra) {
				extraData.putVarInt(key)
				extraData.putLShort(value)
			}
		} else {
			extraData = null
		}
		val stream = ThreadCache.binaryStream.get().reset()
		var count = 0
		val sections: Array<cn.nukkit.level.format.ChunkSection> = chunk.getSections()
		for (i in sections.indices.reversed()) {
			if (!sections[i].isEmpty) {
				count = i + 1
				break
			}
		}
		//        stream.putByte((byte) count);  count is now sent in packet
		for (i in 0 until count) {
			stream.putByte(0.toByte())
			stream.put(sections[i].bytes)
		}
		//        for (byte height : chunk.getHeightMapArray()) {
//            stream.putByte(height);
//        } computed client side?
		stream.put(chunk.getBiomeIdArray())
		stream.putByte(0.toByte())
		if (extraData != null) {
			stream.put(extraData.buffer)
		} else {
			stream.putVarInt(0)
		}
		stream.put(blockEntities)
		this.getLevel().chunkRequestCallback(timestamp, x, z, count, stream.buffer)
		return null
	}

	private var lastPosition = 0
	override fun doGarbageCollection(time: Long) {
		val start = System.currentTimeMillis()
		val maxIterations = size()
		if (lastPosition > maxIterations) lastPosition = 0
		var i: Int
		synchronized(chunks) {
			var iter = chunks.values.iterator()
			if (lastPosition != 0) iter.skip(lastPosition)
			i = 0
			while (i < maxIterations) {
				if (!iter.hasNext()) {
					iter = chunks.values.iterator()
				}
				if (!iter.hasNext()) break
				val chunk = iter.next()
				if (chunk == null) {
					i++
					continue
				}
				if (chunk.isGenerated && chunk.isPopulated && chunk is Chunk) {
					val anvilChunk = chunk
					chunk.compress()
					if (System.currentTimeMillis() - start >= time) break
				}
				i++
			}
		}
		lastPosition += i
	}

	@Synchronized
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

	@Synchronized
	override fun saveChunk(X: Int, Z: Int) {
		val chunk = this.getChunk(X, Z)
		if (chunk != null) {
			try {
				loadRegion(X shr 5, Z shr 5).writeChunk(chunk)
			} catch (e: Exception) {
				throw ChunkException("Error saving chunk ($X, $Z)", e)
			}
		}
	}

	@Synchronized
	override fun saveChunk(x: Int, z: Int, chunk: FullChunk?) {
		if (chunk !is Chunk) {
			throw ChunkException("Invalid Chunk class")
		}
		val regionX = x shr 5
		val regionZ = z shr 5
		loadRegion(regionX, regionZ)
		chunk.x = x
		chunk.z = z
		try {
			getRegion(regionX, regionZ).writeChunk(chunk)
		} catch (e: Exception) {
			throw RuntimeException(e)
		}
	}

	@Synchronized
	protected fun loadRegion(x: Int, z: Int): BaseRegionLoader {
		val tmp = lastRegion.get()
		if (tmp != null && x == tmp.x && z == tmp.z) {
			return tmp
		}
		val index = Level.chunkHash(x, z)
		synchronized(regions) {
			var region = regions[index]
			if (region == null) {
				region = try {
					RegionLoader(this, x, z)
				} catch (e: IOException) {
					throw RuntimeException(e)
				}
				regions[index] = region
			}
			lastRegion.set(region)
			return region
		}
	}

	companion object {
		const val VERSION = 19133
		private val PAD_256 = ByteArray(256)
		val providerName: String
			get() = "anvil"

		val providerOrder: Byte
			get() = LevelProvider.ORDER_YZX

		fun usesChunkSection(): Boolean {
			return true
		}

		fun isValid(path: String): Boolean {
			var isValid = File("$path/level.dat").exists() && File("$path/region/").isDirectory
			if (isValid) {
				for (file in File("$path/region/").listFiles { dir: File?, name: String? -> Pattern.matches("^.+\\.mc[r|a]$", name) }) {
					if (!file.name.endsWith(".mca")) {
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
					.putInt("version", VERSION)
					.putLong("Time", 0)
					.putLong("SizeOnDisk", 0)
			NBTIO.writeGZIPCompressed(CompoundTag().putCompound("Data", levelData), FileOutputStream(path + "level.dat"), ByteOrder.BIG_ENDIAN)
		}

		fun createChunkSection(y: Int): ChunkSection {
			val cs = ChunkSection(y)
			cs.hasSkyLight = true
			return cs
		}
	}
}