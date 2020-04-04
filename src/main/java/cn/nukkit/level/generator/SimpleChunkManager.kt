package cn.nukkit.level.generator

import cn.nukkit.level.ChunkManager
import cn.nukkit.level.format.FullChunk
import cn.nukkit.level.generator

/**
 * author: MagicDroidX
 * Nukkit Project
 */
abstract class SimpleChunkManager(override var seed: Long) : ChunkManager {
	override fun getBlockIdAt(x: Int, y: Int, z: Int): Int {
		val chunk: FullChunk? = getChunk(x shr 4, z shr 4)
		return chunk?.getBlockId(x and 0xf, y and 0xff, z and 0xf) ?: 0
	}

	override fun setBlockIdAt(x: Int, y: Int, z: Int, id: Int) {
		val chunk: FullChunk? = getChunk(x shr 4, z shr 4)
		chunk?.setBlockId(x and 0xf, y and 0xff, z and 0xf, id)
	}

	override fun setBlockAt(x: Int, y: Int, z: Int, id: Int, data: Int) {
		val chunk: FullChunk? = getChunk(x shr 4, z shr 4)
		chunk?.setBlock(x and 0xf, y and 0xff, z and 0xf, id, data)
	}

	override fun setBlockFullIdAt(x: Int, y: Int, z: Int, fullId: Int) {
		val chunk: FullChunk? = getChunk(x shr 4, z shr 4)
		chunk?.setFullBlockId(x and 0xf, y and 0xff, z and 0xf, fullId)
	}

	override fun getBlockDataAt(x: Int, y: Int, z: Int): Int {
		val chunk: FullChunk? = getChunk(x shr 4, z shr 4)
		return chunk?.getBlockData(x and 0xf, y and 0xff, z and 0xf) ?: 0
	}

	override fun setBlockDataAt(x: Int, y: Int, z: Int, data: Int) {
		val chunk: FullChunk? = getChunk(x shr 4, z shr 4)
		chunk?.setBlockData(x and 0xf, y and 0xff, z and 0xf, data)
	}

	override fun setChunk(chunkX: Int, chunkZ: Int) {
		this.setChunk(chunkX, chunkZ, null)
	}

	open fun cleanChunks(seed: Long) {
		this.seed = seed
	}

}