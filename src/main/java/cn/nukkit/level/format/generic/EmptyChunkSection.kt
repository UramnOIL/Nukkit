package cn.nukkit.level.format.generic

import cn.nukkit.block.Block
import cn.nukkit.block.Block.Companion.get
import cn.nukkit.level.format.ChunkSection
import cn.nukkit.utils.ChunkException
import java.util.*

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EmptyChunkSection(private override val y: Int) : ChunkSection {
	companion object {
		val EMPTY = arrayOfNulls<EmptyChunkSection>(16)
		var EMPTY_LIGHT_ARR = ByteArray(2048)
		var EMPTY_SKY_LIGHT_ARR = ByteArray(2048)

		init {
			for (y in EMPTY.indices) {
				EMPTY[y] = EmptyChunkSection(y)
			}
		}

		init {
			Arrays.fill(EMPTY_SKY_LIGHT_ARR, 255.toByte())
		}
	}

	override fun getY(): Int {
		return y
	}

	override fun getBlockId(x: Int, y: Int, z: Int): Int {
		return 0
	}

	@Throws(ChunkException::class)
	override fun getFullBlock(x: Int, y: Int, z: Int): Int {
		return 0
	}

	override fun getAndSetBlock(x: Int, y: Int, z: Int, block: Block?): Block? {
		if (block!!.id != 0) throw ChunkException("Tried to modify an empty Chunk")
		return get(0)
	}

	@Throws(ChunkException::class)
	override fun setBlock(x: Int, y: Int, z: Int, blockId: Int): Boolean {
		if (blockId != 0) throw ChunkException("Tried to modify an empty Chunk")
		return false
	}

	@Throws(ChunkException::class)
	override fun setBlock(x: Int, y: Int, z: Int, blockId: Int, meta: Int): Boolean {
		if (blockId != 0) throw ChunkException("Tried to modify an empty Chunk")
		return false
	}

	override fun getIdArray(): ByteArray {
		return ByteArray(4096)
	}

	override fun getDataArray(): ByteArray {
		return ByteArray(2048)
	}

	override fun getSkyLightArray(): ByteArray {
		return EMPTY_SKY_LIGHT_ARR
	}

	override fun getLightArray(): ByteArray {
		return EMPTY_LIGHT_ARR
	}

	@Throws(ChunkException::class)
	override fun setBlockId(x: Int, y: Int, z: Int, id: Int) {
		if (id != 0) throw ChunkException("Tried to modify an empty Chunk")
	}

	override fun getBlockData(x: Int, y: Int, z: Int): Int {
		return 0
	}

	@Throws(ChunkException::class)
	override fun setBlockData(x: Int, y: Int, z: Int, data: Int) {
		if (data != 0) throw ChunkException("Tried to modify an empty Chunk")
	}

	override fun setFullBlockId(x: Int, y: Int, z: Int, fullId: Int): Boolean {
		if (fullId != 0) throw ChunkException("Tried to modify an empty Chunk")
		return false
	}

	override fun getBlockLight(x: Int, y: Int, z: Int): Int {
		return 0
	}

	@Throws(ChunkException::class)
	override fun setBlockLight(x: Int, y: Int, z: Int, level: Int) {
		if (level != 0) throw ChunkException("Tried to modify an empty Chunk")
	}

	override fun getBlockSkyLight(x: Int, y: Int, z: Int): Int {
		return 15
	}

	@Throws(ChunkException::class)
	override fun setBlockSkyLight(x: Int, y: Int, z: Int, level: Int) {
		if (level != 15) throw ChunkException("Tried to modify an empty Chunk")
	}

	override fun isEmpty(): Boolean {
		return true
	}

	override fun getBytes(): ByteArray {
		return ByteArray(6144)
	}

	override fun copy(): EmptyChunkSection? {
		return this
	}

}