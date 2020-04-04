package cn.nukkit.level.format.leveldb.key

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class EntitiesKey protected constructor(chunkX: Int, chunkZ: Int) : BaseKey(chunkX, chunkZ, BaseKey.Companion.DATA_ENTITIES) {
	companion object {
		fun create(chunkX: Int, chunkZ: Int): EntitiesKey {
			return EntitiesKey(chunkX, chunkZ)
		}
	}
}