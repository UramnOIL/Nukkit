package cn.nukkit.level.format.leveldb.key

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class TilesKey protected constructor(chunkX: Int, chunkZ: Int) : BaseKey(chunkX, chunkZ, BaseKey.Companion.DATA_TILES) {
	companion object {
		fun create(chunkX: Int, chunkZ: Int): TilesKey {
			return TilesKey(chunkX, chunkZ)
		}
	}
}