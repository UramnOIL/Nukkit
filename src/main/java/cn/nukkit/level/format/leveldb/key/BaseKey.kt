package cn.nukkit.level.format.leveldb.key

/**
 * author: MagicDroidX
 * Nukkit Project
 */
open class BaseKey protected constructor(private val chunkX: Int, private val chunkZ: Int, private val type: Byte) {
	fun toArray(): ByteArray {
		return byteArrayOf(
				(chunkX and 0xff).toByte(),
				(chunkX ushr 8 and 0xff).toByte(),
				(chunkX ushr 16 and 0xff).toByte(),
				(chunkX ushr 24 and 0xff).toByte(),
				(chunkZ and 0xff).toByte(),
				(chunkZ ushr 8 and 0xff).toByte(),
				(chunkZ ushr 16 and 0xff).toByte(),
				(chunkZ ushr 24 and 0xff).toByte(),
				type
		)
	}

	companion object {
		const val DATA_VERSION: Byte = 0x76
		const val DATA_FLAGS: Byte = 0x66
		const val DATA_EXTRA_DATA: Byte = 0x34
		const val DATA_TICKS: Byte = 0x33
		const val DATA_ENTITIES: Byte = 0x32
		const val DATA_TILES: Byte = 0x31
		const val DATA_TERRAIN: Byte = 0x30
	}

}