package cn.nukkit.network.protocol

import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

@ToString
class MoveEntityDeltaPacket : DataPacket() {
	var flags = 0
	var xDelta = 0
	var yDelta = 0
	var zDelta = 0
	var yawDelta = 0.0
	var headYawDelta = 0.0
	var pitchDelta = 0.0

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	@Override
	override fun decode() {
		flags = this.getByte()
		xDelta = getCoordinate(FLAG_HAS_X)
		yDelta = getCoordinate(FLAG_HAS_Y)
		zDelta = getCoordinate(FLAG_HAS_Z)
		yawDelta = getRotation(FLAG_HAS_YAW)
		headYawDelta = getRotation(FLAG_HAS_HEAD_YAW)
		pitchDelta = getRotation(FLAG_HAS_PITCH)
	}

	@Override
	override fun encode() {
		this.putByte(flags.toByte())
		putCoordinate(FLAG_HAS_X, xDelta)
		putCoordinate(FLAG_HAS_Y, yDelta)
		putCoordinate(FLAG_HAS_Z, zDelta)
		putRotation(FLAG_HAS_YAW, yawDelta)
		putRotation(FLAG_HAS_HEAD_YAW, headYawDelta)
		putRotation(FLAG_HAS_PITCH, pitchDelta)
	}

	private fun getCoordinate(flag: Int): Int {
		return if (flags and flag != 0) {
			this.getVarInt()
		} else 0
	}

	private fun getRotation(flag: Int): Double {
		return if (flags and flag != 0) {
			this.getByte() * (360.0 / 256.0)
		} else 0.0
	}

	private fun putCoordinate(flag: Int, value: Int) {
		if (flags and flag != 0) {
			this.putVarInt(value)
		}
	}

	private fun putRotation(flag: Int, value: Double) {
		if (flags and flag != 0) {
			this.putByte((value / (360.0 / 256.0)).toByte())
		}
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.MOVE_ENTITY_DELTA_PACKET
		const val FLAG_HAS_X = 1
		const val FLAG_HAS_Y = 2
		const val FLAG_HAS_Z = 4
		const val FLAG_HAS_YAW = 8
		const val FLAG_HAS_HEAD_YAW = 16
		const val FLAG_HAS_PITCH = 32
	}
}