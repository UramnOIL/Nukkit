package cn.nukkit.level

import cn.nukkit.level.format.generic.BaseFullChunk

/**
 * author: MagicDroidX
 * Nukkit Project
 */
interface ChunkManager {
	fun getBlockIdAt(x: Int, y: Int, z: Int): Int
	fun setBlockFullIdAt(x: Int, y: Int, z: Int, fullId: Int)
	fun setBlockIdAt(x: Int, y: Int, z: Int, id: Int)
	fun setBlockAt(x: Int, y: Int, z: Int, id: Int) {
		setBlockAt(x, y, z, id, 0)
	}

	fun setBlockAt(x: Int, y: Int, z: Int, id: Int, data: Int)
	fun getBlockDataAt(x: Int, y: Int, z: Int): Int
	fun setBlockDataAt(x: Int, y: Int, z: Int, data: Int)
	fun getChunk(chunkX: Int, chunkZ: Int): BaseFullChunk?
	fun setChunk(chunkX: Int, chunkZ: Int)
	fun setChunk(chunkX: Int, chunkZ: Int, chunk: BaseFullChunk?)
	val seed: Long
}