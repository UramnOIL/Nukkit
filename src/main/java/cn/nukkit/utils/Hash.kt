package cn.nukkit.utils

object Hash {
	@JvmStatic
	fun hashBlock(x: Int, y: Int, z: Int): Long {
		return y + (x.toLong() and 0x3FFFFFF shl 8) + (z.toLong() and 0x3FFFFFF shl 34)
	}

	@JvmStatic
	fun hashBlockX(triple: Long): Int {
		return (triple shr 8 and 0x3FFFFFF shl 38 shr 38).toInt()
	}

	@JvmStatic
	fun hashBlockY(triple: Long): Int {
		return (triple and 0xFF).toInt()
	}

	@JvmStatic
	fun hashBlockZ(triple: Long): Int {
		return (triple shr 34 and 0x3FFFFFF shl 38 shr 38).toInt()
	}
}