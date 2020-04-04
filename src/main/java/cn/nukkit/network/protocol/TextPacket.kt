package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * Created on 15-10-13.
 */
@ToString
class TextPacket : DataPacket() {
	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	var type: Byte = 0
	var source: String? = ""
	var message: String? = ""
	var parameters: Array<String?>? = arrayOfNulls<String?>(0)
	var isLocalized = false
	var xboxUserId: String? = ""
	var platformChatId: String? = ""

	@Override
	override fun decode() {
		type = getByte() as Byte
		isLocalized = this.getBoolean() || type == TYPE_TRANSLATION
		when (type) {
			TYPE_CHAT, TYPE_WHISPER, TYPE_ANNOUNCEMENT -> {
				source = this.getString()
				message = this.getString()
			}
			TYPE_RAW, TYPE_TIP, TYPE_SYSTEM, TYPE_JSON -> message = this.getString()
			TYPE_TRANSLATION, TYPE_POPUP, TYPE_JUKEBOX_POPUP -> {
				message = this.getString()
				val count = this.getUnsignedVarInt() as Int
				parameters = arrayOfNulls<String?>(count)
				var i = 0
				while (i < count) {
					parameters!![i] = this.getString()
					i++
				}
			}
		}
		xboxUserId = this.getString()
		platformChatId = this.getString()
	}

	@Override
	override fun encode() {
		this.reset()
		this.putByte(type)
		this.putBoolean(isLocalized || type == TYPE_TRANSLATION)
		when (type) {
			TYPE_CHAT, TYPE_WHISPER, TYPE_ANNOUNCEMENT -> {
				this.putString(source)
				this.putString(message)
			}
			TYPE_RAW, TYPE_TIP, TYPE_SYSTEM, TYPE_JSON -> this.putString(message)
			TYPE_TRANSLATION, TYPE_POPUP, TYPE_JUKEBOX_POPUP -> {
				this.putString(message)
				this.putUnsignedVarInt(parameters!!.size)
				for (parameter in parameters!!) {
					this.putString(parameter)
				}
			}
		}
		this.putString(xboxUserId)
		this.putString(platformChatId)
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.TEXT_PACKET
		const val TYPE_RAW: Byte = 0
		const val TYPE_CHAT: Byte = 1
		const val TYPE_TRANSLATION: Byte = 2
		const val TYPE_POPUP: Byte = 3
		const val TYPE_JUKEBOX_POPUP: Byte = 4
		const val TYPE_TIP: Byte = 5
		const val TYPE_SYSTEM: Byte = 6
		const val TYPE_WHISPER: Byte = 7
		const val TYPE_ANNOUNCEMENT: Byte = 8
		const val TYPE_JSON: Byte = 9
	}
}