package cn.nukkit.network.protocol

import cn.nukkit.entity.data.Skin
import lombok.ToString
import java.util.UUID
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * @author Nukkit Project Team
 */
@ToString
class PlayerListPacket : DataPacket() {
	var type: Byte = 0
	var entries: Array<Entry?>? = arrayOfNulls<Entry?>(0)

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		this.putByte(type)
		this.putUnsignedVarInt(entries!!.size)
		for (entry in entries!!) {
			this.putUUID(entry!!.uuid)
			if (type == TYPE_ADD) {
				this.putVarLong(entry!!.entityId)
				this.putString(entry!!.name)
				this.putString(entry!!.xboxUserId)
				this.putString(entry!!.platformChatId)
				this.putLInt(entry!!.buildPlatform)
				this.putSkin(entry!!.skin)
				this.putBoolean(entry!!.isTeacher)
				this.putBoolean(entry!!.isHost)
			}
		}
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	@ToString
	class Entry {
		val uuid: UUID?
		var entityId: Long = 0
		var name: String? = ""
		var xboxUserId: String? = "" //TODO
		var platformChatId: String? = "" //TODO
		var buildPlatform = -1
		var skin: Skin? = null
		var isTeacher = false
		var isHost = false

		constructor(uuid: UUID?) {
			this.uuid = uuid
		}

		constructor(uuid: UUID?, entityId: Long, name: String?, skin: Skin?) : this(uuid, entityId, name, skin, "") {}
		constructor(uuid: UUID?, entityId: Long, name: String?, skin: Skin?, xboxUserId: String?) {
			this.uuid = uuid
			this.entityId = entityId
			this.name = name
			this.skin = skin
			this.xboxUserId = xboxUserId ?: ""
		}
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.PLAYER_LIST_PACKET
		const val TYPE_ADD: Byte = 0
		const val TYPE_REMOVE: Byte = 1
	}
}