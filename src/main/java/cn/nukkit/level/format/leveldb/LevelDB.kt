package cn.nukkit.level.format.leveldb

import cn.nukkit.Server
import cn.nukkit.blockentity.BlockEntitySpawnable
import cn.nukkit.level.GameRules
import cn.nukkit.level.GameRules.Companion.default
import cn.nukkit.level.Level
import cn.nukkit.level.format.ChunkSection
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.format.LevelProvider
import cn.nukkit.level.format.generic.BaseFullChunk
import cn.nukkit.level.format.leveldb.key.BaseKey
import cn.nukkit.level.format.leveldb.key.FlagsKey
import cn.nukkit.level.format.leveldb.key.TerrainKey
import cn.nukkit.level.format.leveldb.key.VersionKey
import cn.nukkit.level.generator.Generator
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.scheduler.AsyncTask
import cn.nukkit.utils.*
import org.iq80.leveldb.DB
import org.iq80.leveldb.Options
import org.iq80.leveldb.impl.Iq80DBFactory
import java.io.*
import java.nio.ByteOrder
import java.util.*
import java.util.function.Consumer
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.set
import kotlin.collections.toTypedArray

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class LevelDB(override var level: Level?, override val path: String) : LevelProvider {
	protected var chunks: MutableMap<Long, Chunk?> = HashMap()
	var database: DB? = null
		protected set
	var levelData: CompoundTag? = null
		protected set

	override fun saveLevelData() {
		try {
			val data = NBTIO.write(levelData, ByteOrder.LITTLE_ENDIAN)
			val outputStream = ByteArrayOutputStream()
			outputStream.write(Binary.writeLInt(3))
			outputStream.write(Binary.writeLInt(data.size))
			outputStream.write(data)
			Utils.writeFile(path + "level.dat", ByteArrayInputStream(outputStream.toByteArray()))
		} catch (e: IOException) {
			throw RuntimeException(e)
		}
	}

	override fun getEmptyChunk(chunkX: Int, chunkZ: Int): Chunk? {
		return Chunk.Companion.getEmptyChunk(chunkX, chunkZ, this)
	}

	override fun requestChunkTask(x: Int, z: Int): AsyncTask? {
		val chunk = this.getChunk(x, z, false) ?: throw ChunkException("Invalid Chunk sent")
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
				NBTIO.write(tagList, ByteOrder.LITTLE_ENDIAN)
			} catch (e: IOException) {
				throw RuntimeException(e)
			}
		}
		val extra: Map<Int, Int> = chunk.getBlockExtraDataArray()
		val extraData: BinaryStream?
		if (!extra.isEmpty()) {
			extraData = BinaryStream()
			extraData.putLInt(extra.size)
			for (key in extra.values) {
				extraData.putLInt(key)
				extraData.putLShort(extra[key]!!)
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
		level!!.chunkRequestCallback(timestamp, x, z, 16, stream.buffer)
		return null
	}

	override fun unloadChunks() {
		for (chunk in ArrayList(chunks.values)) {
			this.unloadChunk(chunk!!.getX(), chunk!!.getZ(), false)
		}
		chunks = HashMap()
	}

	override val generator: String
		get() = levelData!!.getString("generatorName")

	override val generatorOptions: Map<String, Any>
		get() = object : HashMap<String?, Any?>() {
			init {
				put("preset", levelData!!.getString("generatorOptions"))
			}
		}

	override fun getLoadedChunk(X: Int, Z: Int): BaseFullChunk? {
		return this.getLoadedChunk(Level.chunkHash(X, Z))
	}

	override fun getLoadedChunk(hash: Long): BaseFullChunk? {
		return chunks[hash]
	}

	override val loadedChunks: Map<Long, Chunk?>
		get() = chunks

	override fun isChunkLoaded(x: Int, z: Int): Boolean {
		return this.isChunkLoaded(Level.chunkHash(x, z))
	}

	override fun isChunkLoaded(hash: Long): Boolean {
		return chunks.containsKey(hash)
	}

	override fun saveChunks() {
		for (chunk in chunks.values) {
			this.saveChunk(chunk!!.getX(), chunk!!.getZ())
		}
	}

	override fun loadChunk(x: Int, z: Int): Boolean {
		return this.loadChunk(x, z, false)
	}

	override fun loadChunk(x: Int, z: Int, create: Boolean): Boolean {
		val index = Level.chunkHash(x, z)
		if (chunks.containsKey(index)) {
			return true
		}
		level!!.timings.syncChunkLoadDataTimer.startTiming()
		var chunk = readChunk(x, z)
		if (chunk == null && create) {
			chunk = Chunk.Companion.getEmptyChunk(x, z, this)
		}
		level!!.timings.syncChunkLoadDataTimer.stopTiming()
		if (chunk != null) {
			chunks[index] = chunk
			return true
		}
		return false
	}

	fun readChunk(chunkX: Int, chunkZ: Int): Chunk? {
		var data: ByteArray?
		if (!chunkExists(chunkX, chunkZ) || database!![TerrainKey.Companion.create(chunkX, chunkZ).toArray()].also { data = it } == null) {
			return null
		}
		var flags = database!![FlagsKey.Companion.create(chunkX, chunkZ).toArray()]
		if (flags == null) {
			flags = byteArrayOf(0x03)
		}
		return Chunk.Companion.fromBinary(
				Binary.appendBytes(
						Binary.writeLInt(chunkX),
						Binary.writeLInt(chunkZ),
						data,
						flags)
				, this)
	}

	private fun writeChunk(chunk: Chunk?) {
		val binary = chunk!!.toBinary(true)
		database!!.put(TerrainKey.Companion.create(chunk.getX(), chunk.getZ()).toArray(), Binary.subBytes(binary, 8, binary!!.size - 1))
		database!!.put(FlagsKey.Companion.create(chunk.getX(), chunk.getZ()).toArray(), Binary.subBytes(binary, binary.size - 1))
		database!!.put(VersionKey.Companion.create(chunk.getX(), chunk.getZ()).toArray(), byteArrayOf(0x02))
	}

	override fun unloadChunk(x: Int, z: Int): Boolean {
		return this.unloadChunk(x, z, true)
	}

	override fun unloadChunk(x: Int, z: Int, safe: Boolean): Boolean {
		val index = Level.chunkHash(x, z)
		val chunk = chunks.getOrDefault(index, null)
		if (chunk != null && chunk.unload(false, safe)) {
			chunks.remove(index)
			return true
		}
		return false
	}

	override fun saveChunk(x: Int, z: Int) {
		if (this.isChunkLoaded(x, z)) {
			writeChunk(this.getChunk(x, z))
		}
	}

	override fun saveChunk(x: Int, z: Int, chunk: FullChunk?) {
		if (chunk !is Chunk) {
			throw ChunkException("Invalid Chunk class")
		}
		writeChunk(chunk as Chunk?)
	}

	override fun getChunk(x: Int, z: Int): Chunk? {
		return this.getChunk(x, z, false)
	}

	override fun getChunk(x: Int, z: Int, create: Boolean): Chunk? {
		val index = Level.chunkHash(x, z)
		return if (chunks.containsKey(index)) {
			chunks[index]
		} else {
			this.loadChunk(x, z, create)
			chunks.getOrDefault(index, null)
		}
	}

	override fun setChunk(chunkX: Int, chunkZ: Int, chunk: FullChunk?) {
		if (chunk !is Chunk) {
			throw ChunkException("Invalid Chunk class")
		}
		chunk.provider = this
		chunk.setPosition(chunkX, chunkZ)
		val index = Level.chunkHash(chunkX, chunkZ)
		if (chunks.containsKey(index) && chunks[index] != chunk) {
			this.unloadChunk(chunkX, chunkZ, false)
		}
		chunks[index] = chunk as Chunk?
	}

	private fun chunkExists(chunkX: Int, chunkZ: Int): Boolean {
		return database!![VersionKey.Companion.create(chunkX, chunkZ).toArray()] != null
	}

	override fun isChunkGenerated(x: Int, z: Int): Boolean {
		return chunkExists(x, z) && this.getChunk(x, z, false) != null
	}

	override fun isChunkPopulated(x: Int, z: Int): Boolean {
		return this.getChunk(x, z) != null
	}

	override fun close() {
		unloadChunks()
		try {
			database!!.close()
		} catch (e: IOException) {
			throw RuntimeException(e)
		}
		level = null
	}

	val server: Server
		get() = level!!.server

	override val name: String
		get() = levelData!!.getString("LevelName")

	override var isRaining: Boolean
		get() = levelData!!.getFloat("rainLevel") > 0
		set(raining) {
			levelData!!.putFloat("rainLevel", if (raining) 1.0f else 0)
		}

	override var rainTime: Int
		get() = levelData!!.getInt("rainTime")
		set(rainTime) {
			levelData!!.putInt("rainTime", rainTime)
		}

	override var isThundering: Boolean
		get() = levelData!!.getFloat("lightningLevel") > 0
		set(thundering) {
			levelData!!.putFloat("lightningLevel", if (thundering) 1.0f else 0)
		}

	override var thunderTime: Int
		get() = levelData!!.getInt("lightningTime")
		set(thunderTime) {
			levelData!!.putInt("lightningTime", thunderTime)
		}

	override var currentTick: Long
		get() = levelData!!.getLong("currentTick")
		set(currentTick) {
			levelData!!.putLong("currentTick", currentTick)
		}

	override var time: Long
		get() = levelData!!.getLong("Time")
		set(value) {
			levelData!!.putLong("Time", value)
		}

	override var seed: Long
		get() = levelData!!.getLong("RandomSeed")
		set(value) {
			levelData!!.putLong("RandomSeed", value)
		}

	override var spawn: Vector3
		get() = Vector3(levelData!!.getInt("SpawnX").toDouble(), levelData!!.getInt("SpawnY").toDouble(), levelData!!.getInt("SpawnZ").toDouble())
		set(pos) {
			levelData!!.putInt("SpawnX", pos.x as Int)
			levelData!!.putInt("SpawnY", pos.y as Int)
			levelData!!.putInt("SpawnZ", pos.z as Int)
		}

	override val gamerules: GameRules
		get() {
			val rules = default
			if (levelData!!.contains("GameRules")) rules.readNBT(levelData!!.getCompound("GameRules"))
			return rules
		}

	override fun setGameRules(rules: GameRules?) {
		levelData!!.putCompound("GameRules", rules!!.writeNBT())
	}

	override fun doGarbageCollection() {}

	override fun updateLevelName(name: String?) {
		if (this.name != name) {
			levelData!!.putString("LevelName", name)
		}
	}

	val terrainKeys: Array<ByteArray>
		get() {
			val result: MutableList<ByteArray> = ArrayList()
			database!!.forEach(Consumer { entry: Map.Entry<ByteArray, ByteArray?> ->
				val key = entry.key
				if (key.size > 8 && key[8] == BaseKey.Companion.DATA_TERRAIN) {
					result.add(key)
				}
			})
			return result.toTypedArray()
		}

	companion object {
		val providerName: String
			get() = "leveldb"

		val providerOrder: Byte
			get() = LevelProvider.ORDER_ZXY

		fun usesChunkSection(): Boolean {
			return false
		}

		fun isValid(path: String): Boolean {
			return File("$path/level.dat").exists() && File("$path/db").isDirectory
		}

		@JvmOverloads
		@Throws(IOException::class)
		fun generate(path: String, name: String?, seed: Long, generator: Class<out Generator?>?, options: Map<String?, String?>? = HashMap()) {
			if (!File("$path/db").exists()) {
				File("$path/db").mkdirs()
			}
			val levelData = CompoundTag("")
					.putLong("currentTick", 0)
					.putInt("DayCycleStopTime", -1)
					.putInt("GameType", 0)
					.putInt("Generator", Generator.getGeneratorType(generator))
					.putBoolean("hasBeenLoadedInCreative", false)
					.putLong("LastPlayed", System.currentTimeMillis() / 1000)
					.putString("LevelName", name)
					.putFloat("lightningLevel", 0f)
					.putInt("lightningTime", Random().nextInt())
					.putInt("limitedWorldOriginX", 128)
					.putInt("limitedWorldOriginY", 70)
					.putInt("limitedWorldOriginZ", 128)
					.putInt("Platform", 0)
					.putFloat("rainLevel", 0f)
					.putInt("rainTime", Random().nextInt())
					.putLong("RandomSeed", seed)
					.putByte("spawnMobs", 0)
					.putInt("SpawnX", 128)
					.putInt("SpawnY", 70)
					.putInt("SpawnZ", 128)
					.putInt("storageVersion", 4)
					.putLong("Time", 0)
					.putLong("worldStartCount", Int.MAX_VALUE.toLong() and 0xffffffffL)
			val data = NBTIO.write(levelData, ByteOrder.LITTLE_ENDIAN)
			val outputStream = ByteArrayOutputStream()
			outputStream.write(Binary.writeLInt(3))
			outputStream.write(Binary.writeLInt(data.size))
			outputStream.write(data)
			Utils.writeFile(path + "level.dat", ByteArrayInputStream(outputStream.toByteArray()))
			val db = Iq80DBFactory.factory.open(File("$path/db"), Options().createIfMissing(true))
			db.close()
		}

		fun createChunkSection(y: Int): ChunkSection? {
			return null
		}
	}

	init {
		val file_path = File(path)
		if (!file_path.exists()) {
			file_path.mkdirs()
		}
		try {
			FileInputStream(path + "level.dat").use { stream ->
				stream.skip(8)
				val levelData = NBTIO.read(stream, ByteOrder.LITTLE_ENDIAN)
				if (levelData != null) {
					this.levelData = levelData
				} else {
					throw IOException("LevelData can not be null")
				}
			}
		} catch (e: IOException) {
			throw LevelException("Invalid level.dat")
		}
		if (!levelData!!.contains("generatorName")) {
			levelData!!.putString("generatorName", Generator.getGenerator("DEFAULT").simpleName.toLowerCase())
		}
		if (!levelData!!.contains("generatorOptions")) {
			levelData!!.putString("generatorOptions", "")
		}
		try {
			database = Iq80DBFactory.factory.open(File(path + "/db"), Options().createIfMissing(true))
		} catch (e: IOException) {
			throw RuntimeException(e)
		}
	}
}