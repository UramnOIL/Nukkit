package cn.nukkit.level.generator

import cn.nukkit.level.format.generic.BaseFullChunk
import cn.nukkit.level.generator

class SingleChunkManager(seed: Long) : SimpleChunkManager(seed) {
	private var CX = Int.MAX_VALUE
	private var CZ = Int.MAX_VALUE
	private var chunk: BaseFullChunk? = null
	override fun getChunk(chunkX: Int, chunkZ: Int): BaseFullChunk? {
		return if (chunkX == CX && chunkZ == CZ) {
			chunk
		} else null
	}

	override fun setChunk(chunkX: Int, chunkZ: Int, chunk: BaseFullChunk?) {
		if (chunk == null) {
			this.chunk = null
			CX = Int.MAX_VALUE
			CZ = Int.MAX_VALUE
		} else if (this.chunk != null) {
			throw UnsupportedOperationException("Replacing chunks is not allowed behavior")
		} else {
			this.chunk = chunk
			CX = chunk.getX()
			CZ = chunk.getZ()
		}
	}

	override fun cleanChunks(seed: Long) {
		super.cleanChunks(seed)
		chunk = null
		CX = Int.MAX_VALUE
		CZ = Int.MAX_VALUE
	}
}