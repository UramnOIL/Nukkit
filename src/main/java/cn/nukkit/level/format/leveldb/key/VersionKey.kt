package cn.nukkit.level.format.leveldb.key

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class VersionKey protected constructor(chunkX: Int, chunkZ: Int) : BaseKey(chunkX, chunkZ, BaseKey.Companion.DATA_VERSION) {
	companion object {
		fun create(chunkX: Int, chunkZ: Int): VersionKey {
			return VersionKey(chunkX, chunkZ)
		}
	}
}