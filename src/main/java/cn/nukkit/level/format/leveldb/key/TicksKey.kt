package cn.nukkit.level.format.leveldb.key

/**
 * author: MagicDroidX
 * Nukkit Project
 */
class TicksKey protected constructor(chunkX: Int, chunkZ: Int) : BaseKey(chunkX, chunkZ, BaseKey.Companion.DATA_TICKS) {
	companion object {
		fun create(chunkX: Int, chunkZ: Int): TicksKey {
			return TicksKey(chunkX, chunkZ)
		}
	}
}