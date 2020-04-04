package cn.nukkit.level.format

/**
 * author: MagicDroidX
 * Nukkit Project
 */
interface Chunk : FullChunk {
	fun isSectionEmpty(fY: Float): Boolean
	fun getSection(fY: Float): ChunkSection?
	fun setSection(fY: Float, section: ChunkSection?): Boolean
	val sections: Array<ChunkSection?>?

	class Entry(val chunkX: Int, val chunkZ: Int)

	companion object {
		const val SECTION_COUNT: Byte = 16
	}
}