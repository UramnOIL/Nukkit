package cn.nukkit.level.format.leveldb.key

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class ExtraDataKey protected constructor(chunkX: Int, chunkZ: Int) : BaseKey(chunkX, chunkZ, BaseKey.Companion.DATA_EXTRA_DATA) {
	companion object {
		fun create(chunkX: Int, chunkZ: Int): ExtraDataKey {
			return ExtraDataKey(chunkX, chunkZ)
		}
	}
}