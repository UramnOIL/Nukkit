package cn.nukkit.utils

import cn.nukkit.entity.data.Skin
import lombok.ToString
import java.util.*

@ToString(exclude = ["data"])
class SerializedImage(val width: Int, val height: Int, val data: ByteArray) {

	companion object {
		val EMPTY = SerializedImage(0, 0, ByteArray(0))
		@JvmStatic
		fun fromLegacy(skinData: ByteArray): SerializedImage {
			Objects.requireNonNull(skinData, "skinData")
			when (skinData.size) {
				Skin.SINGLE_SKIN_SIZE -> return SerializedImage(64, 32, skinData)
				Skin.DOUBLE_SKIN_SIZE -> return SerializedImage(64, 64, skinData)
				Skin.SKIN_128_64_SIZE -> return SerializedImage(128, 64, skinData)
				Skin.SKIN_128_128_SIZE -> return SerializedImage(128, 128, skinData)
			}
			throw IllegalArgumentException("Unknown legacy skin size")
		}
	}

}