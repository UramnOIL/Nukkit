package cn.nukkit.network.protocol

import cn.nukkit.entity.data.Skin
import lombok.ToString
import java.util.UUID
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class PlayerSkinPacket : DataPacket() {
	var uuid: UUID? = null
	var skin: Skin? = null
	var newSkinName: String? = null
	var oldSkinName: String? = null

	@Override
	override fun pid(): Byte {
		return ProtocolInfo.PLAYER_SKIN_PACKET
	}

	@Override
	override fun decode() {
		uuid = getUUID()
		skin = getSkin()
		newSkinName = getString()
		oldSkinName = getString()
	}

	@Override
	override fun encode() {
		reset()
		putUUID(uuid)
		putSkin(skin)
		putString(newSkinName)
		putString(oldSkinName)
	}
}