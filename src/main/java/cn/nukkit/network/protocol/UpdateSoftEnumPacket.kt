package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class UpdateSoftEnumPacket : DataPacket() {
	val values: Array<String?>? = arrayOfNulls<String?>(0)
	var name: String? = ""
	var type: Type? = Type.SET

	@Override
	override fun pid(): Byte {
		return ProtocolInfo.UPDATE_SOFT_ENUM_PACKET
	}

	@Override
	override fun decode() {
	}

	@Override
	override fun encode() {
		this.reset()
		this.putString(name)
		this.putUnsignedVarInt(values!!.size)
		for (value in values!!) {
			this.putString(value)
		}
		this.putByte(type!!.ordinal() as Byte)
	}

	enum class Type {
		ADD, REMOVE, SET
	}
}