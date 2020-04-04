package cn.nukkit.level.format.leveldb.key

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class FlagsKey protected constructor(chunkX: Int, chunkZ: Int) : BaseKey(chunkX, chunkZ, BaseKey.Companion.DATA_FLAGS) {
	companion object {
		fun create(chunkX: Int, chunkZ: Int): FlagsKey {
			return FlagsKey(chunkX, chunkZ)
		}
	}
}