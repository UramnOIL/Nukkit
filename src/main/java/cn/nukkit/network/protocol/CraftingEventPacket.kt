package cn.nukkit.network.protocol

import cn.nukkit.item.Item
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
class CraftingEventPacket : DataPacket() {
	var windowId = 0
	var type = 0
	var id: UUID? = null
	var input: Array<Item?>?
	var output: Array<Item?>?

	@Override
	override fun decode() {
		windowId = this.getByte()
		type = this.getVarInt()
		id = this.getUUID()
		val inputSize = this.getUnsignedVarInt() as Int
		input = arrayOfNulls<Item?>(inputSize)
		run {
			var i = 0
			while (i < inputSize && i < 128) {
				input!![i] = this.getSlot()
				++i
			}
		}
		val outputSize = this.getUnsignedVarInt() as Int
		output = arrayOfNulls<Item?>(outputSize)
		var i = 0
		while (i < outputSize && i < 128) {
			output!![i] = getSlot()
			++i
		}
	}

	@Override
	override fun encode() {
	}

	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.CRAFTING_EVENT_PACKET
		const val TYPE_SHAPELESS = 0
		const val TYPE_SHAPED = 1
		const val TYPE_FURNACE = 2
		const val TYPE_FURNACE_DATA = 3
		const val TYPE_MULTI = 4
		const val TYPE_SHULKER_BOX = 5
	}
}