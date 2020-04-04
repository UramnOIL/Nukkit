package cn.nukkit.level.format.generic

import cn.nukkit.Server
import cn.nukkit.level.GameRules
import cn.nukkit.level.GameRules.Companion.default
import cn.nukkit.level.Level
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.format.LevelProvider
import cn.nukkit.level.generator.Generator
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.utils.ChunkException
import cn.nukkit.utils.LevelException
import com.google.common.collect.ImmutableMap
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.atomic.AtomicReference

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BaseLevelProvider(override var level: Level?, override val path: String) : LevelProvider {
	var levelData: CompoundTag? = null
		protected set
	private override var spawn: Vector3
	protected val lastRegion = AtomicReference<BaseRegionLoader?>()
	protected val regions: Long2ObjectMap<BaseRegionLoader> = Long2ObjectOpenHashMap()
	protected val chunks: Long2ObjectMap<BaseFullChunk> = Long2ObjectOpenHashMap()
	private val lastChunk = AtomicReference<BaseFullChunk?>()
	abstract fun loadChunk(index: Long, chunkX: Int, chunkZ: Int, create: Boolean): BaseFullChunk?
	fun size(): Int {
		synchronized(chunks) { return chunks.size() }
	}

	override fun unloadChunks() {
		val iter = chunks.values.iterator()
		while (iter.hasNext()) {
			iter.next().unload(true, false)
			iter.remove()
		}
	}

	override val generator: String
		get() = levelData!!.getString("generatorName")

	override val generatorOptions: Map<String, Any>
		get() = object : HashMap<String?, Any?>() {
			init {
				put("preset", levelData!!.getString("generatorOptions"))
			}
		}

	override val loadedChunks: Map<Long, BaseFullChunk>
		get() {
			synchronized(chunks) { return ImmutableMap.copyOf(chunks) }
		}

	override fun isChunkLoaded(X: Int, Z: Int): Boolean {
		return isChunkLoaded(Level.chunkHash(X, Z))
	}

	fun putChunk(index: Long, chunk: BaseFullChunk) {
		synchronized(chunks) { chunks.put(index, chunk) }
	}

	override fun isChunkLoaded(hash: Long): Boolean {
		synchronized(chunks) { return chunks.containsKey(hash) }
	}

	fun getRegion(x: Int, z: Int): BaseRegionLoader {
		val index = Level.chunkHash(x, z)
		synchronized(regions) { return regions[index] }
	}

	val server: Server
		get() = level!!.server

	override val name: String
		get() = levelData!!.getString("LevelName")

	override var isRaining: Boolean
		get() = levelData!!.getBoolean("raining")
		set(raining) {
			levelData!!.putBoolean("raining", raining)
		}

	override var rainTime: Int
		get() = levelData!!.getInt("rainTime")
		set(rainTime) {
			levelData!!.putInt("rainTime", rainTime)
		}

	override var isThundering: Boolean
		get() = levelData!!.getBoolean("thundering")
		set(thundering) {
			levelData!!.putBoolean("thundering", thundering)
		}

	override var thunderTime: Int
		get() = levelData!!.getInt("thunderTime")
		set(thunderTime) {
			levelData!!.putInt("thunderTime", thunderTime)
		}

	override var currentTick: Long
		get() = levelData!!.getLong("Time")
		set(currentTick) {
			levelData!!.putLong("Time", currentTick)
		}

	override var time: Long
		get() = levelData!!.getLong("DayTime")
		set(value) {
			levelData!!.putLong("DayTime", value)
		}

	override var seed: Long
		get() = levelData!!.getLong("RandomSeed")
		set(value) {
			levelData!!.putLong("RandomSeed", value)
		}

	override fun getSpawn(): Vector3 {
		return spawn
	}

	override fun setSpawn(pos: Vector3) {
		levelData!!.putInt("SpawnX", pos.x as Int)
		levelData!!.putInt("SpawnY", pos.y as Int)
		levelData!!.putInt("SpawnZ", pos.z as Int)
		spawn = pos
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

	override fun doGarbageCollection() {
		val limit = (System.currentTimeMillis() - 50).toInt()
		synchronized(regions) {
			if (regions.isEmpty()) {
				return
			}
			val iter = regions.values.iterator()
			while (iter.hasNext()) {
				val loader = iter.next()
				if (loader.lastUsed <= limit) {
					try {
						loader.close()
					} catch (e: IOException) {
						throw RuntimeException("Unable to close RegionLoader", e)
					}
					lastRegion.set(null)
					iter.remove()
				}
			}
		}
	}

	override fun saveChunks() {
		synchronized(chunks) {
			for (chunk in chunks.values) {
				if (chunk.getChanges() != 0L) {
					chunk.setChanged(false)
					this.saveChunk(chunk.getX(), chunk.getZ())
				}
			}
		}
	}

	override fun saveLevelData() {
		try {
			NBTIO.writeGZIPCompressed(CompoundTag().putCompound("Data", levelData), FileOutputStream(path + "level.dat"))
		} catch (e: IOException) {
			throw RuntimeException(e)
		}
	}

	override fun updateLevelName(name: String?) {
		if (this.name != name) {
			levelData!!.putString("LevelName", name)
		}
	}

	override fun loadChunk(chunkX: Int, chunkZ: Int): Boolean {
		return this.loadChunk(chunkX, chunkZ, false)
	}

	override fun loadChunk(chunkX: Int, chunkZ: Int, create: Boolean): Boolean {
		val index = Level.chunkHash(chunkX, chunkZ)
		synchronized(chunks) {
			if (chunks.containsKey(index)) {
				return true
			}
		}
		return loadChunk(index, chunkX, chunkZ, create) != null
	}

	override fun unloadChunk(X: Int, Z: Int): Boolean {
		return this.unloadChunk(X, Z, true)
	}

	override fun unloadChunk(X: Int, Z: Int, safe: Boolean): Boolean {
		val index = Level.chunkHash(X, Z)
		synchronized(chunks) {
			val chunk = chunks[index]
			if (chunk != null && chunk.unload(false, safe)) {
				lastChunk.set(null)
				chunks.remove(index, chunk)
				return true
			}
		}
		return false
	}

	override fun getChunk(chunkX: Int, chunkZ: Int): BaseFullChunk? {
		return this.getChunk(chunkX, chunkZ, false)
	}

	override fun getLoadedChunk(chunkX: Int, chunkZ: Int): BaseFullChunk? {
		var tmp = lastChunk.get()
		if (tmp != null && tmp.getX() == chunkX && tmp.getZ() == chunkZ) {
			return tmp
		}
		val index = Level.chunkHash(chunkX, chunkZ)
		synchronized(chunks) { lastChunk.set(chunks[index].also { tmp = it }) }
		return tmp
	}

	override fun getLoadedChunk(hash: Long): BaseFullChunk? {
		var tmp = lastChunk.get()
		if (tmp != null && tmp.getIndex() == hash) {
			return tmp
		}
		synchronized(chunks) { lastChunk.set(chunks[hash].also { tmp = it }) }
		return tmp
	}

	override fun getChunk(chunkX: Int, chunkZ: Int, create: Boolean): BaseFullChunk? {
		var tmp = lastChunk.get()
		if (tmp != null && tmp.getX() == chunkX && tmp.getZ() == chunkZ) {
			return tmp
		}
		val index = Level.chunkHash(chunkX, chunkZ)
		synchronized(chunks) { lastChunk.set(chunks[index].also { tmp = it }) }
		return if (tmp != null) {
			tmp
		} else {
			tmp = this.loadChunk(index, chunkX, chunkZ, create)
			lastChunk.set(tmp)
			tmp
		}
	}

	override fun setChunk(chunkX: Int, chunkZ: Int, chunk: FullChunk?) {
		if (chunk !is BaseFullChunk) {
			throw ChunkException("Invalid Chunk class")
		}
		chunk.provider = this
		chunk.setPosition(chunkX, chunkZ)
		val index = Level.chunkHash(chunkX, chunkZ)
		synchronized(chunks) {
			if (chunks.containsKey(index) && chunks[index] != chunk) {
				this.unloadChunk(chunkX, chunkZ, false)
			}
			chunks.put(index, chunk)
		}
	}

	override fun isChunkPopulated(chunkX: Int, chunkZ: Int): Boolean {
		val chunk = this.getChunk(chunkX, chunkZ)
		return chunk != null && chunk.isPopulated
	}

	@Synchronized
	override fun close() {
		unloadChunks()
		synchronized(regions) {
			val iter = regions.values.iterator()
			while (iter.hasNext()) {
				try {
					iter.next().close()
				} catch (e: IOException) {
					throw RuntimeException("Unable to close RegionLoader", e)
				}
				lastRegion.set(null)
				iter.remove()
			}
		}
		level = null
	}

	override fun isChunkGenerated(chunkX: Int, chunkZ: Int): Boolean {
		val region = getRegion(chunkX shr 5, chunkZ shr 5)
		return region != null && region.chunkExists(chunkX - region.getX() * 32, chunkZ - region.getZ() * 32) && this.getChunk(chunkX - region.getX() * 32, chunkZ - region.getZ() * 32, true)!!.isGenerated
	}

	companion object {
		protected fun getRegionIndexX(chunkX: Int): Int {
			return chunkX shr 5
		}

		protected fun getRegionIndexZ(chunkZ: Int): Int {
			return chunkZ shr 5
		}
	}

	init {
		val file_path = File(path)
		if (!file_path.exists()) {
			file_path.mkdirs()
		}
		val levelData = NBTIO.readCompressed(FileInputStream(File(path + "level.dat")), ByteOrder.BIG_ENDIAN)
		if (levelData["Data"] is CompoundTag) {
			this.levelData = levelData.getCompound("Data")
		} else {
			throw LevelException("Invalid level.dat")
		}
		if (!this.levelData.contains("generatorName")) {
			this.levelData.putString("generatorName", Generator.getGenerator("DEFAULT").simpleName.toLowerCase())
		}
		if (!this.levelData.contains("generatorOptions")) {
			this.levelData.putString("generatorOptions", "")
		}
		spawn = Vector3(this.levelData.getInt("SpawnX").toDouble(), this.levelData.getInt("SpawnY").toDouble(), this.levelData.getInt("SpawnZ").toDouble())
	}
}