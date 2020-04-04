package cn.nukkit.level

import cn.nukkit.level.format.FullChunk
import cn.nukkit.math.Vector3

/**
 * author: MagicDroidX
 * Nukkit Project
 */
interface ChunkLoader {
	val loaderId: Int
	val isLoaderActive: Boolean
	val position: Position?
	val x: Double
	val z: Double
	val level: Level?
	fun onChunkChanged(chunk: FullChunk?)
	fun onChunkLoaded(chunk: FullChunk?)
	fun onChunkUnloaded(chunk: FullChunk?)
	fun onChunkPopulated(chunk: FullChunk?)
	fun onBlockChanged(block: Vector3?)
}