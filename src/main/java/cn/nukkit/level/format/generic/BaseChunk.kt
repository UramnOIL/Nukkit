package cn.nukkit.level.format.generic

import cn.nukkit.Server
import cn.nukkit.block.Block
import cn.nukkit.level.format.Chunk
import cn.nukkit.level.format.ChunkSection
import cn.nukkit.level.format.LevelProvider
import cn.nukkit.utils.ChunkException
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.nio.ByteBuffer
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class BaseChunk : BaseFullChunk(), Chunk {
	override var sections: Array<ChunkSection?>?
	override fun clone(): BaseChunk {
		val chunk = super.clone() as BaseChunk
		if (biomes != null) chunk.biomes = biomes.clone()
		chunk.heightMap = heightMapArray!!.clone()
		if (sections != null && sections!![0] != null) {
			chunk.sections = arrayOfNulls(sections!!.size)
			for (i in sections!!.indices) {
				chunk.sections!![i] = sections!![i]!!.copy()
			}
		}
		return chunk
	}

	private fun removeInvalidTile(x: Int, y: Int, z: Int) {
		val entity = getTile(x, y, z)
		if (entity != null && !entity.isBlockEntityValid) {
			removeBlockEntity(entity)
		}
	}

	override fun getFullBlock(x: Int, y: Int, z: Int): Int {
		return sections!![y shr 4]!!.getFullBlock(x, y and 0x0f, z)
	}

	override fun setBlock(x: Int, y: Int, z: Int, blockId: Int): Boolean {
		return this.setBlock(x, y, z, blockId, 0)
	}

	override fun getAndSetBlock(x: Int, y: Int, z: Int, block: Block?): Block? {
		val Y = y shr 4
		return try {
			setChanged()
			sections!![Y]!!.getAndSetBlock(x, y and 0x0f, z, block)
		} catch (e: ChunkException) {
			try {
				setInternalSection(Y.toFloat(), providerClass!!.getMethod("createChunkSection", Int::class.javaPrimitiveType).invoke(providerClass, Y) as ChunkSection)
			} catch (e1: IllegalAccessException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: InvocationTargetException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: NoSuchMethodException) {
				Server.instance!!.logger.logException(e1)
			}
			sections!![Y]!!.getAndSetBlock(x, y and 0x0f, z, block)
		} finally {
			removeInvalidTile(x, y, z)
		}
	}

	override fun setFullBlockId(x: Int, y: Int, z: Int, fullId: Int): Boolean {
		val Y = y shr 4
		return try {
			setChanged()
			sections!![Y]!!.setFullBlockId(x, y and 0x0f, z, fullId)
		} catch (e: ChunkException) {
			try {
				setInternalSection(Y.toFloat(), providerClass!!.getMethod("createChunkSection", Int::class.javaPrimitiveType).invoke(providerClass, Y) as ChunkSection)
			} catch (e1: IllegalAccessException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: InvocationTargetException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: NoSuchMethodException) {
				Server.instance!!.logger.logException(e1)
			}
			sections!![Y]!!.setFullBlockId(x, y and 0x0f, z, fullId)
		} finally {
			removeInvalidTile(x, y, z)
		}
	}

	override fun setBlock(x: Int, y: Int, z: Int, blockId: Int, meta: Int): Boolean {
		val Y = y shr 4
		return try {
			setChanged()
			sections!![Y]!!.setBlock(x, y and 0x0f, z, blockId, meta)
		} catch (e: ChunkException) {
			try {
				setInternalSection(Y.toFloat(), providerClass!!.getMethod("createChunkSection", Int::class.javaPrimitiveType).invoke(providerClass, Y) as ChunkSection)
			} catch (e1: IllegalAccessException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: InvocationTargetException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: NoSuchMethodException) {
				Server.instance!!.logger.logException(e1)
			}
			sections!![Y]!!.setBlock(x, y and 0x0f, z, blockId, meta)
		} finally {
			removeInvalidTile(x, y, z)
		}
	}

	override fun setBlockId(x: Int, y: Int, z: Int, id: Int) {
		val Y = y shr 4
		try {
			sections!![Y]!!.setBlockId(x, y and 0x0f, z, id)
			setChanged()
		} catch (e: ChunkException) {
			try {
				setInternalSection(Y.toFloat(), providerClass!!.getMethod("createChunkSection", Int::class.javaPrimitiveType).invoke(providerClass, Y) as ChunkSection)
			} catch (e1: IllegalAccessException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: InvocationTargetException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: NoSuchMethodException) {
				Server.instance!!.logger.logException(e1)
			}
			sections!![Y]!!.setBlockId(x, y and 0x0f, z, id)
		} finally {
			removeInvalidTile(x, y, z)
		}
	}

	override fun getBlockId(x: Int, y: Int, z: Int): Int {
		return sections!![y shr 4]!!.getBlockId(x, y and 0x0f, z)
	}

	override fun getBlockData(x: Int, y: Int, z: Int): Int {
		return sections!![y shr 4]!!.getBlockData(x, y and 0x0f, z)
	}

	override fun setBlockData(x: Int, y: Int, z: Int, data: Int) {
		val Y = y shr 4
		try {
			sections!![Y]!!.setBlockData(x, y and 0x0f, z, data)
			setChanged()
		} catch (e: ChunkException) {
			try {
				setInternalSection(Y.toFloat(), providerClass!!.getMethod("createChunkSection", Int::class.javaPrimitiveType).invoke(providerClass, Y) as ChunkSection)
			} catch (e1: IllegalAccessException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: InvocationTargetException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: NoSuchMethodException) {
				Server.instance!!.logger.logException(e1)
			}
			sections!![Y]!!.setBlockData(x, y and 0x0f, z, data)
		} finally {
			removeInvalidTile(x, y, z)
		}
	}

	override fun getBlockSkyLight(x: Int, y: Int, z: Int): Int {
		return sections!![y shr 4]!!.getBlockSkyLight(x, y and 0x0f, z)
	}

	override fun setBlockSkyLight(x: Int, y: Int, z: Int, level: Int) {
		val Y = y shr 4
		try {
			sections!![Y]!!.setBlockSkyLight(x, y and 0x0f, z, level)
			setChanged()
		} catch (e: ChunkException) {
			try {
				setInternalSection(Y.toFloat(), providerClass!!.getMethod("createChunkSection", Int::class.javaPrimitiveType).invoke(providerClass, Y) as ChunkSection)
			} catch (e1: IllegalAccessException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: InvocationTargetException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: NoSuchMethodException) {
				Server.instance!!.logger.logException(e1)
			}
			sections!![Y]!!.setBlockSkyLight(x, y and 0x0f, z, level)
		}
	}

	override fun getBlockLight(x: Int, y: Int, z: Int): Int {
		return sections!![y shr 4]!!.getBlockLight(x, y and 0x0f, z)
	}

	override fun setBlockLight(x: Int, y: Int, z: Int, level: Int) {
		val Y = y shr 4
		try {
			sections!![Y]!!.setBlockLight(x, y and 0x0f, z, level)
			setChanged()
		} catch (e: ChunkException) {
			try {
				setInternalSection(Y.toFloat(), providerClass!!.getMethod("createChunkSection", Int::class.javaPrimitiveType).invoke(providerClass, Y) as ChunkSection)
			} catch (e1: IllegalAccessException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: InvocationTargetException) {
				Server.instance!!.logger.logException(e1)
			} catch (e1: NoSuchMethodException) {
				Server.instance!!.logger.logException(e1)
			}
			sections!![Y]!!.setBlockLight(x, y and 0x0f, z, level)
		}
	}

	override fun isSectionEmpty(fY: Float): Boolean {
		return sections!![fY.toInt()] is EmptyChunkSection
	}

	override fun getSection(fY: Float): ChunkSection? {
		return sections!![fY.toInt()]
	}

	override fun setSection(fY: Float, section: ChunkSection?): Boolean {
		val emptyIdArray = ByteArray(4096)
		val emptyDataArray = ByteArray(2048)
		if (Arrays.equals(emptyIdArray, section!!.idArray) && Arrays.equals(emptyDataArray, section.dataArray)) {
			sections!![fY.toInt()] = EmptyChunkSection.Companion.EMPTY.get(fY.toInt())
		} else {
			sections!![fY.toInt()] = section
		}
		setChanged()
		return true
	}

	private fun setInternalSection(fY: Float, section: ChunkSection) {
		sections!![fY.toInt()] = section
		setChanged()
	}

	@Throws(IOException::class)
	override fun load(): Boolean {
		return this.load(true)
	}

	@Throws(IOException::class)
	override fun load(generate: Boolean): Boolean {
		return this.provider != null && this.provider.getChunk(this.getX(), this.getZ(), true) != null
	}

	override val blockIdArray: ByteArray?
		get() {
			val buffer = ByteBuffer.allocate(4096 * Chunk.SECTION_COUNT)
			for (y in 0 until Chunk.SECTION_COUNT) {
				buffer.put(sections!![y]!!.idArray)
			}
			return buffer.array()
		}

	override val blockDataArray: ByteArray?
		get() {
			val buffer = ByteBuffer.allocate(2048 * Chunk.SECTION_COUNT)
			for (y in 0 until Chunk.SECTION_COUNT) {
				buffer.put(sections!![y]!!.dataArray)
			}
			return buffer.array()
		}

	override val blockSkyLightArray: ByteArray?
		get() {
			val buffer = ByteBuffer.allocate(2048 * Chunk.SECTION_COUNT)
			for (y in 0 until Chunk.SECTION_COUNT) {
				buffer.put(sections!![y]!!.skyLightArray)
			}
			return buffer.array()
		}

	override val blockLightArray: ByteArray?
		get() {
			val buffer = ByteBuffer.allocate(2048 * Chunk.SECTION_COUNT)
			for (y in 0 until Chunk.SECTION_COUNT) {
				buffer.put(sections!![y]!!.lightArray)
			}
			return buffer.array()
		}

	override val heightMapArray: ByteArray?
		get() = heightMap

	override var provider: LevelProvider?
		set(provider) {
			super.provider = provider
		}
}