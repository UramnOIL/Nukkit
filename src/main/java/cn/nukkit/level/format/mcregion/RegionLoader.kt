package cn.nukkit.level.format.mcregion

import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.format.LevelProvider
import cn.nukkit.level.format.generic.BaseRegionLoader
import cn.nukkit.utils.*
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.MutableMap
import kotlin.collections.set

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class RegionLoader(level: LevelProvider?, regionX: Int, regionZ: Int) : BaseRegionLoader(level, regionX, regionZ, "mcr") {
	override fun isChunkGenerated(index: Int): Boolean {
		val array = locationTable[index]
		return !(array!![0] == 0 || array[1] == 0)
	}

	@Throws(IOException::class)
	override fun readChunk(x: Int, z: Int): Chunk? {
		val index = getChunkOffset(x, z)
		if (index < 0 || index >= 4096) {
			return null
		}
		lastUsed = System.currentTimeMillis()
		if (!isChunkGenerated(index)) {
			return null
		}
		val table = locationTable[index]
		val raf = randomAccessFile
		raf!!.seek((table!![0]!! shl 12.toLong().toInt()).toLong())
		val length = raf.readInt()
		if (length <= 0 || length >= BaseRegionLoader.Companion.MAX_SECTOR_LENGTH) {
			if (length >= BaseRegionLoader.Companion.MAX_SECTOR_LENGTH) {
				table[0] = ++lastSector
				table[1] = 1
				locationTable[index] = table
				MainLogger.getLogger().error("Corrupted chunk header detected")
			}
			return null
		}
		val compression = raf.readByte()
		if (length > table[1]!! shl 12) {
			MainLogger.getLogger().error("Corrupted bigger chunk detected")
			table[1] = length shr 12
			locationTable[index] = table
			writeLocationIndex(index)
		} else if (compression != BaseRegionLoader.Companion.COMPRESSION_ZLIB && compression != BaseRegionLoader.Companion.COMPRESSION_GZIP) {
			MainLogger.getLogger().error("Invalid compression type")
			return null
		}
		val data = ByteArray(length - 1)
		raf.readFully(data)
		val chunk = unserializeChunk(data)
		return if (chunk != null) {
			chunk
		} else {
			MainLogger.getLogger().error("Corrupted chunk detected")
			null
		}
	}

	override fun unserializeChunk(data: ByteArray?): Chunk? {
		return Chunk.Companion.fromBinary(data, levelProvider)
	}

	override fun chunkExists(x: Int, z: Int): Boolean {
		return isChunkGenerated(getChunkOffset(x, z))
	}

	@Throws(IOException::class)
	override fun saveChunk(x: Int, z: Int, chunkData: ByteArray?) {
		val length = chunkData!!.size + 1
		if (length + 4 > BaseRegionLoader.Companion.MAX_SECTOR_LENGTH) {
			throw ChunkException("Chunk is too big! " + (length + 4) + " > " + BaseRegionLoader.Companion.MAX_SECTOR_LENGTH)
		}
		val sectors = Math.ceil((length + 4) / 4096.0).toInt()
		val index = getChunkOffset(x, z)
		var indexChanged = false
		val table = locationTable[index]
		if (table!![1]!! < sectors) {
			table[0] = lastSector + 1
			locationTable[index] = table
			lastSector += sectors
			indexChanged = true
		} else if (table[1] != sectors) {
			indexChanged = true
		}
		table[1] = sectors
		table[2] = (System.currentTimeMillis() / 1000.0).toInt()
		locationTable[index] = table
		val raf = randomAccessFile
		raf!!.seek((table[0]!! shl 12.toLong().toInt()).toLong())
		val stream = BinaryStream()
		stream.put(Binary.writeInt(length))
		stream.putByte(BaseRegionLoader.Companion.COMPRESSION_ZLIB)
		stream.put(chunkData)
		var data = stream.buffer
		if (data.size < sectors shl 12) {
			val newData = ByteArray(sectors shl 12)
			System.arraycopy(data, 0, newData, 0, data.size)
			data = newData
		}
		raf.write(data)
		if (indexChanged) {
			writeLocationIndex(index)
		}
	}

	override fun removeChunk(x: Int, z: Int) {
		val index = getChunkOffset(x, z)
		val table = locationTable[0]
		table!![0] = 0
		table[1] = 0
		locationTable[index] = table
	}

	@Throws(Exception::class)
	override fun writeChunk(chunk: FullChunk?) {
		lastUsed = System.currentTimeMillis()
		val chunkData = chunk!!.toBinary()
		saveChunk(chunk.x and 0x1f, chunk.z and 0x1f, chunkData)
	}

	@Throws(IOException::class)
	override fun close() {
		writeLocationTable()
		levelProvider = null
		super.close()
	}

	@Throws(Exception::class)
	override fun doSlowCleanUp(): Int {
		val raf = randomAccessFile
		for (i in 0..1023) {
			var table = locationTable[i]
			if (table!![0] == 0 || table[1] == 0) {
				continue
			}
			raf!!.seek((table[0]!! shl 12.toLong().toInt()).toLong())
			var chunk = ByteArray(table[1]!! shl 12)
			raf.readFully(chunk)
			val length = Binary.readInt(Arrays.copyOfRange(chunk, 0, 3))
			if (length <= 1) {
				locationTable[i] = arrayOf(0, 0, 0).also { table = it }
			}
			try {
				chunk = Zlib.inflate(Arrays.copyOf(chunk, 5))
			} catch (e: Exception) {
				locationTable[i] = arrayOf<Int?>(0, 0, 0)
				continue
			}
			chunk = Zlib.deflate(chunk, 9)
			val buffer = ByteBuffer.allocate(4 + 1 + chunk.size)
			buffer.put(Binary.writeInt(chunk.size + 1))
			buffer.put(BaseRegionLoader.Companion.COMPRESSION_ZLIB)
			buffer.put(chunk)
			chunk = buffer.array()
			val sectors = Math.ceil(chunk.size / 4096.0).toInt()
			if (sectors > table!![1]!!) {
				table!![0] = lastSector + 1
				lastSector += sectors
				locationTable[i] = table
			}
			raf.seek((table!![0]!! shl 12.toLong().toInt()).toLong())
			val bytes = ByteArray(sectors shl 12)
			val buffer1 = ByteBuffer.wrap(bytes)
			buffer1.put(chunk)
			raf.write(buffer1.array())
		}
		writeLocationTable()
		val n = cleanGarbage()
		writeLocationTable()
		return n
	}

	@Throws(IOException::class)
	override fun loadLocationTable() {
		val raf = randomAccessFile
		raf!!.seek(0)
		lastSector = 1
		val data = IntArray(1024 * 2) //1024 records * 2 times
		for (i in 0 until 1024 * 2) {
			data[i] = raf.readInt()
		}
		for (i in 0..1023) {
			val index = data[i]
			locationTable[i] = arrayOf<Int?>(index shr 8, index and 0xff, data[1024 + i])
			val value = locationTable[i]!![0] + locationTable[i]!![1] - 1
			if (value > lastSector) {
				lastSector = value
			}
		}
	}

	@Throws(IOException::class)
	private fun writeLocationTable() {
		val raf = randomAccessFile
		raf!!.seek(0)
		for (i in 0..1023) {
			val array = locationTable[i]
			raf.writeInt(array!![0]!! shl 8 or array[1]!!)
		}
		for (i in 0..1023) {
			val array = locationTable[i]
			raf.writeInt(array!![2]!!)
		}
	}

	@Throws(IOException::class)
	private fun cleanGarbage(): Int {
		val sectors: MutableMap<Int, Int> = TreeMap()
		for (index in ArrayList(locationTable.keys)) {
			val data = locationTable[index]
			if (data!![0] == 0 || data[1] == 0) {
				locationTable[index] = arrayOf<Int?>(0, 0, 0)
				continue
			}
			sectors[data[0]!!] = index
		}
		if (sectors.size == lastSector - 2) {
			return 0
		}
		var shift = 0
		val lastSector = 1
		val raf = randomAccessFile
		raf!!.seek(8192)
		var s = 2
		for (sector in sectors.keys) {
			s = sector
			val index = sectors[sector]!!
			if (sector - lastSector > 1) {
				shift += sector - lastSector - 1
			}
			if (shift > 0) {
				raf.seek((sector shl 12.toLong().toInt()).toLong())
				val old = ByteArray(4096)
				raf.readFully(old)
				raf.seek((sector - shift shl 12.toLong().toInt()).toLong())
				raf.write(old)
			}
			val v = locationTable[index]
			v!![0]!! -= shift
			locationTable[index] = v
			this.lastSector = sector
		}
		raf.setLength((s + 1 shl 12.toLong().toInt()).toLong())
		return shift
	}

	@Throws(IOException::class)
	override fun writeLocationIndex(index: Int) {
		val array = locationTable[index]
		val raf = randomAccessFile
		raf!!.seek((index shl 2.toLong().toInt()).toLong())
		raf.writeInt(array!![0]!! shl 8 or array[1]!!)
		raf.seek(4096 + (index shl 2).toLong())
		raf.writeInt(array[2]!!)
	}

	@Throws(IOException::class)
	override fun createBlank() {
		val raf = randomAccessFile
		raf!!.seek(0)
		raf.setLength(0)
		lastSector = 1
		val time = (System.currentTimeMillis() / 1000.0).toInt()
		for (i in 0..1023) {
			locationTable[i] = arrayOf<Int?>(0, 0, time)
			raf.writeInt(0)
		}
		for (i in 0..1023) {
			raf.writeInt(time)
		}
	}

	override fun getX(): Int {
		return x
	}

	override fun getZ(): Int {
		return z
	}

	companion object {
		protected fun getChunkOffset(x: Int, z: Int): Int {
			return x or (z shl 5)
		}
	}
}