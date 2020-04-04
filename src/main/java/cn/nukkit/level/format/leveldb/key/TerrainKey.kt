package cn.nukkit.level.format.leveldb.key

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class TerrainKey protected constructor(chunkX: Int, chunkZ: Int) : BaseKey(chunkX, chunkZ, BaseKey.Companion.DATA_TERRAIN) {
	companion object {
		fun create(chunkX: Int, chunkZ: Int): TerrainKey {
			return TerrainKey(chunkX, chunkZ)
		}
	}
}