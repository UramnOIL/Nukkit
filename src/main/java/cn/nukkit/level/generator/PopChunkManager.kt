package cn.nukkit.level.generator

import cn.nukkit.level.format.generic.BaseFullChunk
import cn.nukkit.level.generator
import java.util.*

class PopChunkManager(seed: Long) : SimpleChunkManager(seed) {
	private var clean = true
	private val chunks = arrayOfNulls<BaseFullChunk>(9)
	private var CX = Int.MAX_VALUE
	private var CZ = Int.MAX_VALUE
	override fun cleanChunks(seed: Long) {
		super.cleanChunks(seed)
		if (!clean) {
			Arrays.fill(chunks, null)
			CX = Int.MAX_VALUE
			CZ = Int.MAX_VALUE
			clean = true
		}
	}

	override fun getChunk(chunkX: Int, chunkZ: Int): BaseFullChunk? {
		var index: Int
		index = when (chunkX - CX) {
			0 -> 0
			1 -> 1
			2 -> 2
			else -> return null
		}
		when (chunkZ - CZ) {
			0 -> {
			}
			1 -> index += 3
			2 -> index += 6
			else -> return null
		}
		return chunks[index]
	}

	override fun setChunk(chunkX: Int, chunkZ: Int, chunk: BaseFullChunk?) {
		if (CX == Int.MAX_VALUE) {
			CX = chunkX
			CZ = chunkZ
		}
		var index: Int
		index = when (chunkX - CX) {
			0 -> 0
			1 -> 1
			2 -> 2
			else -> throw UnsupportedOperationException("Chunk is outside population area")
		}
		when (chunkZ - CZ) {
			0 -> {
			}
			1 -> index += 3
			2 -> index += 6
			else -> throw UnsupportedOperationException("Chunk is outside population area")
		}
		clean = false
		chunks[index] = chunk
	}
}