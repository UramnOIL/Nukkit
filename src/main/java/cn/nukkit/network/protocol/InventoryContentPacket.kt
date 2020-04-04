package cn.nukkit.network.protocol

import cn.nukkit.item.Item
import lombok.ToString
import kotlin.jvm.Volatile
import kotlin.jvm.Throws
import cn.nukkit.network.protocol.types.CommandOriginData.Origin
import CommandOriginData.Origin

/**
 * author: MagicDroidX
 * Nukkit Project
 */
@ToString
class InventoryContentPacket : DataPacket() {
	@Override
	override fun pid(): Byte {
		return NETWORK_ID
	}

	var inventoryId = 0
	var slots: Array<Item?>? = arrayOfNulls<Item?>(0)

	@Override
	override fun clean(): DataPacket? {
		slots = arrayOfNulls<Item?>(0)
		return super.clean()
	}

	@Override
	override fun decode() {
		inventoryId = this.getUnsignedVarInt() as Int
		val count = this.getUnsignedVarInt() as Int
		slots = arrayOfNulls<Item?>(count)
		var s = 0
		while (s < count && !this.feof()) {
			slots!![s] = this.getSlot()
			++s
		}
	}

	@Override
	override fun encode() {
		this.reset()
		this.putUnsignedVarInt(inventoryId)
		this.putUnsignedVarInt(slots!!.size)
		for (slot in slots!!) {
			this.putSlot(slot)
		}
	}

	@Override
	override fun clone(): InventoryContentPacket? {
		val pk = super.clone() as InventoryContentPacket?
		pk!!.slots = slots.clone()
		return pk
	}

	companion object {
		val NETWORK_ID: Byte = ProtocolInfo.INVENTORY_CONTENT_PACKET
		const val SPECIAL_INVENTORY = 0
		const val SPECIAL_OFFHAND = 0x77
		const val SPECIAL_ARMOR = 0x78
		const val SPECIAL_CREATIVE = 0x79
		const val SPECIAL_HOTBAR = 0x7a
		const val SPECIAL_FIXED_INVENTORY = 0x7b
	}
}