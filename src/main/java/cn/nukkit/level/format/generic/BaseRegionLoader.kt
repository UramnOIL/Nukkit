package cn.nukkit.level.format.generic

import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.format.LevelProvider
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BaseRegionLoader(level: LevelProvider?, regionX: Int, regionZ: Int, ext: String) {
	protected var x = 0
	protected var z = 0
	protected var lastSector = 0
	protected var levelProvider: LevelProvider? = null
	var randomAccessFile: RandomAccessFile? = null

	// TODO: A simple array will perform better and use less memory
	protected val locationTable: Map<Int, Array<Int>> = HashMap()
	var lastUsed: Long = 0
	fun compress() {
		// TODO
	}

	protected abstract fun isChunkGenerated(index: Int): Boolean

	@Throws(IOException::class)
	abstract fun readChunk(x: Int, z: Int): BaseFullChunk?
	protected abstract fun unserializeChunk(data: ByteArray?): BaseFullChunk?
	abstract fun chunkExists(x: Int, z: Int): Boolean

	@Throws(IOException::class)
	protected abstract fun saveChunk(x: Int, z: Int, chunkData: ByteArray?)
	abstract fun removeChunk(x: Int, z: Int)

	@Throws(Exception::class)
	abstract fun writeChunk(chunk: FullChunk?)

	@Throws(IOException::class)
	open fun close() {
		randomAccessFile?.close()
	}

	@Throws(IOException::class)
	protected abstract fun loadLocationTable()

	@Throws(Exception::class)
	abstract fun doSlowCleanUp(): Int

	@Throws(IOException::class)
	protected abstract fun writeLocationIndex(index: Int)

	@Throws(IOException::class)
	protected abstract fun createBlank()
	abstract fun getX(): Int
	abstract fun getZ(): Int
	val locationIndexes: Array<Int>
		get() = locationTable.keys.toTypedArray()

	companion object {
		const val VERSION = 1
		const val COMPRESSION_GZIP: Byte = 1
		const val COMPRESSION_ZLIB: Byte = 2
		const val MAX_SECTOR_LENGTH = 256 shl 12
		const val COMPRESSION_LEVEL = 7
	}

	init {
		try {
			x = regionX
			z = regionZ
			levelProvider = level
			val filePath = levelProvider!!.path.toString() + "region/r." + regionX + "." + regionZ + "." + ext
			val file = File(filePath)
			val exists = file.exists()
			if (!exists) {
				file.createNewFile()
			}
			// TODO: buffering is a temporary solution to chunk reading/writing being poorly optimized
			//  - need to fix the code where it reads single bytes at a time from disk
			randomAccessFile = RandomAccessFile(filePath, "rw")
			if (!exists) {
				createBlank()
			} else {
				loadLocationTable()
			}
			lastUsed = System.currentTimeMillis()
		} catch (e: IOException) {
			throw RuntimeException(e)
		}
	}
}