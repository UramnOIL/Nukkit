package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class ModalFormResponsePacket : DataPacket() {
	var formId = 0
	var data: String? = null

	@Override
	override fun pid(): Byte {
		return ProtocolInfo.MODAL_FORM_RESPONSE_PACKET
	}

	@Override
	override fun decode() {
		formId = this.getVarInt()
		data = this.getString() //Data will be null if player close form without submit (by cross button or ESC)
	}

	@Override
	override fun encode() {
	}
}